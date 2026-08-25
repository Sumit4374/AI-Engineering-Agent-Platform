package sumit.ai.ai_engineering.agent.model;

import java.util.Map;

public record PlannedStep(
    int stepNumber,
    String description,
    String toolToUse,
    Map<String, Object> toolArguments,
    boolean useRag
) {}
