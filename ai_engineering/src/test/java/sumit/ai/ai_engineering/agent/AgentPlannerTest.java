package sumit.ai.ai_engineering.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import sumit.ai.ai_engineering.agent.model.AgentPlan;
import sumit.ai.ai_engineering.agent.planner.AgentPlannerImpl;
import sumit.ai.ai_engineering.ai.engine.AIEngine;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

@ExtendWith(MockitoExtension.class)
class AgentPlannerTest {

    @Mock private AIEngine aiEngine;

    private AgentPlannerImpl planner;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        planner = new AgentPlannerImpl(aiEngine, objectMapper);
    }

    @Test
    void createPlan_validJsonFromEngine_parsesStructuredPlan() throws Exception {
        String jsonOutput = """
                {
                  "goal": "Inspect build config",
                  "reasoning": "Check pom.xml first",
                  "steps": [
                    {
                      "stepNumber": 1,
                      "description": "Read pom.xml",
                      "toolToUse": "readFile",
                      "toolArguments": {"relativePath": "pom.xml"},
                      "useRag": false
                    }
                  ]
                }
                """;

        when(aiEngine.generate(any(), eq("AGENT_PLANNING.prompt"), any(), any(ToolsCategory[].class)))
                .thenReturn(jsonOutput);

        AgentPlan plan = planner.createPlan("Inspect build config");

        assertThat(plan.goal()).isEqualTo("Inspect build config");
        assertThat(plan.reasoning()).isEqualTo("Check pom.xml first");
        assertThat(plan.steps()).hasSize(1);
        assertThat(plan.steps().get(0).toolToUse()).isEqualTo("readFile");
    }

    @Test
    void createPlan_rawMarkdownJson_cleansAndParsesPlan() throws Exception {
        String rawOutput = """
                ```json
                {
                  "goal": "Find architecture",
                  "reasoning": "Inspect tree",
                  "steps": [
                    {
                      "stepNumber": 1,
                      "description": "Inspect tree",
                      "toolToUse": "tree",
                      "toolArguments": {},
                      "useRag": false
                    }
                  ]
                }
                ```
                """;

        when(aiEngine.generate(any(), eq("AGENT_PLANNING.prompt"), any(), any(ToolsCategory[].class)))
                .thenReturn(rawOutput);

        AgentPlan plan = planner.createPlan("Find architecture");

        assertThat(plan.steps()).hasSize(1);
        assertThat(plan.steps().get(0).toolToUse()).isEqualTo("tree");
    }

    @Test
    void createPlan_engineFails_returnsHeuristicDefaultPlan() throws Exception {
        when(aiEngine.generate(any(), eq("AGENT_PLANNING.prompt"), any(), any(ToolsCategory[].class)))
                .thenThrow(new RuntimeException("Engine offline"));

        AgentPlan plan = planner.createPlan("Analyze project");

        assertThat(plan.steps()).isNotEmpty();
        assertThat(plan.steps().get(0).toolToUse()).isEqualTo("analyzeProject");
    }
}
