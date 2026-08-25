package sumit.ai.ai_engineering.agent.model;

import java.util.List;

public record AgentPlan(
    String goal,
    String reasoning,
    List<PlannedStep> steps
) {}
