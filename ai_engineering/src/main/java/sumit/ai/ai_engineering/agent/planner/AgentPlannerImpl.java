package sumit.ai.ai_engineering.agent.planner;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sumit.ai.ai_engineering.agent.model.AgentPlan;
import sumit.ai.ai_engineering.agent.model.PlannedStep;
import sumit.ai.ai_engineering.ai.engine.AIEngine;
import sumit.ai.ai_engineering.ai.prompt.PromptType;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

@Service
public class AgentPlannerImpl implements AgentPlanner {

    private static final Logger log = LoggerFactory.getLogger(AgentPlannerImpl.class);

    private final AIEngine aiEngine;
    private final ObjectMapper objectMapper;

    public AgentPlannerImpl(AIEngine aiEngine, ObjectMapper objectMapper) {
        this.aiEngine = aiEngine;
        this.objectMapper = objectMapper;
    }

    @Override
    public AgentPlan createPlan(String goal) {
        if (goal == null || goal.isBlank()) {
            throw new IllegalArgumentException("Goal must not be blank");
        }

        try {
            String rawOutput = aiEngine.generate(
                    null,
                    PromptType.AGENT_PLANNING.getFileName(),
                    Map.of("goal", goal),
                    ToolsCategory.DOCUMENTATION, ToolsCategory.UTILITY
            );

            // Clean markdown code fence if present
            String cleaned = cleanJsonString(rawOutput);
            JsonNode root = objectMapper.readTree(cleaned);

            String reasoning = root.path("reasoning").asText("Autonomous task decomposition");
            JsonNode stepsNode = root.path("steps");

            if (stepsNode.isArray() && !stepsNode.isEmpty()) {
                List<PlannedStep> steps = objectMapper.readerForListOf(PlannedStep.class).readValue(stepsNode);
                log.info("Agent planner generated {} steps for goal: [{}]", steps.size(), goal);
                return new AgentPlan(goal, reasoning, steps);
            }
        } catch (Exception e) {
            log.warn("Structured agent planning failed, using heuristic default plan: {}", e.getMessage());
        }

        // Heuristic default plan: 1. Analyze project/context -> 2. Synthesize answer
        PlannedStep step1 = new PlannedStep(1, "Analyze project structure and relevant files", "analyzeProject", Map.of(), false);
        PlannedStep step2 = new PlannedStep(2, "Synthesize final response for goal", null, Map.of(), true);
        return new AgentPlan(goal, "Default direct execution strategy", List.of(step1, step2));
    }

    private String cleanJsonString(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
}
