package sumit.ai.ai_engineering.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import sumit.ai.ai_engineering.agent.executor.AgentExecutorImpl;
import sumit.ai.ai_engineering.agent.model.AgentExecution;
import sumit.ai.ai_engineering.agent.model.AgentExecutionRepository;
import sumit.ai.ai_engineering.agent.model.AgentPlan;
import sumit.ai.ai_engineering.agent.model.AgentStatus;
import sumit.ai.ai_engineering.agent.model.AgentStep;
import sumit.ai.ai_engineering.agent.model.AgentStepRepository;
import sumit.ai.ai_engineering.agent.model.PlannedStep;
import sumit.ai.ai_engineering.ai.engine.AIEngine;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;
import sumit.ai.ai_engineering.events.publisher.EventPublisher;
import sumit.ai.ai_engineering.mcp.model.McpToolCallRequest;
import sumit.ai.ai_engineering.mcp.model.McpToolCallResult;
import sumit.ai.ai_engineering.mcp.server.McpServerService;
import sumit.ai.ai_engineering.rag.retrieval.RagRetrievalService;

@ExtendWith(MockitoExtension.class)
class AgentExecutorTest {

    @Mock private AgentExecutionRepository executionRepository;
    @Mock private AgentStepRepository stepRepository;
    @Mock private McpServerService mcpServerService;
    @Mock private RagRetrievalService ragRetrievalService;
    @Mock private AIEngine aiEngine;
    @Mock private EventPublisher eventPublisher;

    private AgentExecutorImpl executor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Long userId = 1L;
    private final UUID executionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        executor = new AgentExecutorImpl(
                executionRepository,
                stepRepository,
                mcpServerService,
                ragRetrievalService,
                aiEngine,
                eventPublisher,
                objectMapper
        );
    }

    @Test
    void execute_successfulMultiStepPlan_runsStepsAndSynthesizesOutput() throws Exception {
        PlannedStep step1 = new PlannedStep(1, "Read pom.xml", "readFile", Map.of("relativePath", "pom.xml"), false);
        AgentPlan plan = new AgentPlan("Check project", "Inspection", List.of(step1));

        AgentExecution execution = AgentExecution.builder()
                .id(executionId)
                .userId(userId)
                .goal("Check project")
                .status(AgentStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.save(any(AgentExecution.class))).thenAnswer(i -> i.getArgument(0));

        when(mcpServerService.executeTool(any(McpToolCallRequest.class)))
                .thenReturn(McpToolCallResult.success("readFile", "<artifactId>ai_engineering</artifactId>"));

        when(aiEngine.generate(any(), any(), any(), any(ToolsCategory[].class)))
                .thenReturn("Project is a modular monolith.");

        AgentExecution result = executor.execute(userId, executionId, plan, 10, true);

        assertThat(result.getStatus()).isEqualTo(AgentStatus.COMPLETED);
        assertThat(result.getResult()).isEqualTo("Project is a modular monolith.");
        assertThat(result.getIterations()).isEqualTo(1);
        verify(stepRepository).save(any(AgentStep.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    void execute_toolFails_recordsFailureStepAndCompletesGracefully() throws Exception {
        PlannedStep step1 = new PlannedStep(1, "Read missing file", "readFile", Map.of("relativePath", "missing.txt"), false);
        AgentPlan plan = new AgentPlan("Inspect", "Check", List.of(step1));

        AgentExecution execution = AgentExecution.builder()
                .id(executionId)
                .userId(userId)
                .goal("Inspect")
                .status(AgentStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.save(any(AgentExecution.class))).thenAnswer(i -> i.getArgument(0));

        when(mcpServerService.executeTool(any(McpToolCallRequest.class)))
                .thenReturn(McpToolCallResult.error("readFile", "File not found"));

        when(aiEngine.generate(any(), any(), any(), any(ToolsCategory[].class)))
                .thenReturn("Could not find file, completed review.");

        AgentExecution result = executor.execute(userId, executionId, plan, 10, true);

        assertThat(result.getStatus()).isEqualTo(AgentStatus.COMPLETED);
        verify(stepRepository).save(any(AgentStep.class));
    }
}
