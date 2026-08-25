package sumit.ai.ai_engineering.agent.executor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import sumit.ai.ai_engineering.agent.model.AgentExecution;
import sumit.ai.ai_engineering.agent.model.AgentExecutionRepository;
import sumit.ai.ai_engineering.agent.model.AgentPlan;
import sumit.ai.ai_engineering.agent.model.AgentStatus;
import sumit.ai.ai_engineering.agent.model.AgentStep;
import sumit.ai.ai_engineering.agent.model.AgentStepRepository;
import sumit.ai.ai_engineering.agent.model.PlannedStep;
import sumit.ai.ai_engineering.agent.model.StepStatus;
import sumit.ai.ai_engineering.ai.engine.AIEngine;
import sumit.ai.ai_engineering.ai.prompt.PromptType;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;
import sumit.ai.ai_engineering.events.model.AgentExecutionCompletedEvent;
import sumit.ai.ai_engineering.events.publisher.EventPublisher;
import sumit.ai.ai_engineering.mcp.model.McpToolCallRequest;
import sumit.ai.ai_engineering.mcp.model.McpToolCallResult;
import sumit.ai.ai_engineering.mcp.server.McpServerService;
import sumit.ai.ai_engineering.memory.model.MemoryMessage;
import sumit.ai.ai_engineering.rag.model.RetrievedChunk;
import sumit.ai.ai_engineering.rag.retrieval.RagRetrievalService;

@Service
public class AgentExecutorImpl implements AgentExecutor {

    private static final Logger log = LoggerFactory.getLogger(AgentExecutorImpl.class);

    private static final int DEFAULT_MAX_ITERATIONS = 10;
    private static final int MAX_TOKEN_BUDGET = 50_000;
    private static final long EXECUTION_TIMEOUT_MS = 120_000; // 120s

    private final AgentExecutionRepository executionRepository;
    private final AgentStepRepository stepRepository;
    private final McpServerService mcpServerService;
    private final RagRetrievalService ragRetrievalService;
    private final AIEngine aiEngine;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public AgentExecutorImpl(
            AgentExecutionRepository executionRepository,
            AgentStepRepository stepRepository,
            McpServerService mcpServerService,
            RagRetrievalService ragRetrievalService,
            AIEngine aiEngine,
            EventPublisher eventPublisher,
            ObjectMapper objectMapper) {
        this.executionRepository = executionRepository;
        this.stepRepository = stepRepository;
        this.mcpServerService = mcpServerService;
        this.ragRetrievalService = ragRetrievalService;
        this.aiEngine = aiEngine;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AgentExecution execute(Long userId, UUID executionId, AgentPlan plan, int maxIterations, boolean allowRag) {
        long startTime = System.currentTimeMillis();
        int iterationLimit = maxIterations > 0 ? Math.min(maxIterations, 20) : DEFAULT_MAX_ITERATIONS;

        AgentExecution execution = executionRepository.findById(executionId)
                .orElseGet(() -> AgentExecution.builder()
                        .id(executionId)
                        .userId(userId)
                        .goal(plan.goal())
                        .status(AgentStatus.RUNNING)
                        .startedAt(LocalDateTime.now())
                        .build());

        try {
            execution.setPlanJson(objectMapper.writeValueAsString(plan));
        } catch (Exception ignored) {
        }
        execution = executionRepository.save(execution);

        List<String> accumulatedStepObservations = new ArrayList<>();
        int totalTokensEstimated = 0;
        int iteration = 0;

        try {
            for (PlannedStep step : plan.steps()) {
                iteration++;
                if (iteration > iterationLimit) {
                    log.warn("Agent reached max iterations limit ({}) for execution [{}]", iterationLimit, executionId);
                    break;
                }
                if (System.currentTimeMillis() - startTime > EXECUTION_TIMEOUT_MS) {
                    log.warn("Agent reached timeout limit for execution [{}]", executionId);
                    break;
                }
                if (totalTokensEstimated > MAX_TOKEN_BUDGET) {
                    log.warn("Agent reached token budget for execution [{}]", executionId);
                    break;
                }

                long stepStart = System.currentTimeMillis();
                AgentStep agentStep = AgentStep.builder()
                        .id(UUID.randomUUID())
                        .executionId(executionId)
                        .stepIndex(step.stepNumber())
                        .stepName(step.description())
                        .toolName(step.toolToUse())
                        .status(StepStatus.RUNNING)
                        .createdAt(LocalDateTime.now())
                        .build();

                StringBuilder stepOutput = new StringBuilder();

                // 1. Tool execution (if tool requested)
                if (step.toolToUse() != null && !step.toolToUse().isBlank()) {
                    McpToolCallRequest toolReq = new McpToolCallRequest(step.toolToUse(), step.toolArguments());
                    try {
                        agentStep.setInputArgs(objectMapper.writeValueAsString(step.toolArguments()));
                    } catch (Exception ignored) {
                    }

                    McpToolCallResult toolRes = mcpServerService.executeTool(toolReq);
                    if (toolRes.isError()) {
                        stepOutput.append("Tool Execution Error: ").append(toolRes.errorMessage());
                        agentStep.setStatus(StepStatus.FAILED);
                    } else {
                        stepOutput.append("Tool Output: ").append(toolRes.content());
                        agentStep.setStatus(StepStatus.COMPLETED);
                    }
                }

                // 2. RAG retrieval (if step calls for it)
                if (allowRag && step.useRag()) {
                    List<RetrievedChunk> ragChunks = ragRetrievalService.retrieve(userId, step.description(), 3, 0.2);
                    if (!ragChunks.isEmpty()) {
                        if (!stepOutput.isEmpty()) stepOutput.append("\n\n");
                        stepOutput.append("RAG Context:\n").append(ragRetrievalService.formatContext(ragChunks));
                    }
                }

                // 3. Fallback pure reasoning step
                if (stepOutput.isEmpty()) {
                    stepOutput.append("Executed reasoning step: ").append(step.description());
                    agentStep.setStatus(StepStatus.COMPLETED);
                }

                long stepDuration = System.currentTimeMillis() - stepStart;
                agentStep.setDurationMs(stepDuration);
                agentStep.setOutputResult(stepOutput.toString());
                stepRepository.save(agentStep);

                accumulatedStepObservations.add(String.format("Step %d (%s): %s",
                        step.stepNumber(), step.description(), stepOutput));
                totalTokensEstimated += MemoryMessage.estimateTokens(stepOutput.toString());
            }

            // Final synthesis of goal with collected intermediate facts
            String synthesisPrompt = buildSynthesisContext(plan.goal(), plan.reasoning(), accumulatedStepObservations);
            String finalAnswer = aiEngine.generate(
                    executionId.toString(),
                    PromptType.EXPLAIN.getFileName(),
                    Map.of("topic", synthesisPrompt),
                    ToolsCategory.UTILITY
            );

            execution.setStatus(AgentStatus.COMPLETED);
            execution.setIterations(iteration);
            execution.setTokenUsage(totalTokensEstimated + MemoryMessage.estimateTokens(finalAnswer));
            execution.setResult(finalAnswer);
            execution.setCompletedAt(LocalDateTime.now());
            AgentExecution saved = executionRepository.save(execution);

            log.info("Agent execution completed successfully [id={}, userId={}, iterations={}]",
                    executionId, userId, iteration);

            // Publish Kafka event
            eventPublisher.publish(AgentExecutionCompletedEvent.of(
                    userId, executionId, plan.goal(), "COMPLETED", iteration
            ));

            return saved;

        } catch (Exception e) {
            log.error("Agent execution failed for [id={}]: {}", executionId, e.getMessage(), e);
            execution.setStatus(AgentStatus.FAILED);
            execution.setError("Execution failed: " + e.getMessage());
            execution.setCompletedAt(LocalDateTime.now());
            AgentExecution saved = executionRepository.save(execution);

            eventPublisher.publish(AgentExecutionCompletedEvent.of(
                    userId, executionId, plan.goal(), "FAILED", iteration
            ));

            return saved;
        }
    }

    private String buildSynthesisContext(String goal, String reasoning, List<String> observations) {
        StringBuilder sb = new StringBuilder();
        sb.append("User Goal: ").append(goal).append("\n\n");
        sb.append("Plan Strategy: ").append(reasoning).append("\n\n");
        sb.append("Executed Steps and Observations:\n");
        for (String obs : observations) {
            sb.append("- ").append(obs).append("\n");
        }
        sb.append("\nPlease synthesize a comprehensive, structured final answer fulfilling the user's goal based on all the step observations above.");
        return sb.toString();
    }
}
