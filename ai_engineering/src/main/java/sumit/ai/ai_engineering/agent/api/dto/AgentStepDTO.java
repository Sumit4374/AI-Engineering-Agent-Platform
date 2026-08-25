package sumit.ai.ai_engineering.agent.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import sumit.ai.ai_engineering.agent.model.AgentStep;
import sumit.ai.ai_engineering.agent.model.StepStatus;

public record AgentStepDTO(
    UUID id,
    int stepIndex,
    String stepName,
    String toolName,
    String inputArgs,
    String outputResult,
    StepStatus status,
    Long durationMs,
    LocalDateTime createdAt
) {
    public static AgentStepDTO from(AgentStep s) {
        return new AgentStepDTO(
            s.getId(),
            s.getStepIndex(),
            s.getStepName(),
            s.getToolName(),
            s.getInputArgs(),
            s.getOutputResult(),
            s.getStatus(),
            s.getDurationMs(),
            s.getCreatedAt()
        );
    }
}
