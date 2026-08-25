package sumit.ai.ai_engineering.agent.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import sumit.ai.ai_engineering.agent.model.AgentExecution;
import sumit.ai.ai_engineering.agent.model.AgentStatus;

public record AgentExecutionDTO(
    UUID id,
    Long userId,
    String goal,
    AgentStatus status,
    int iterations,
    int tokenUsage,
    LocalDateTime startedAt,
    LocalDateTime completedAt
) {
    public static AgentExecutionDTO from(AgentExecution e) {
        return new AgentExecutionDTO(
            e.getId(),
            e.getUserId(),
            e.getGoal(),
            e.getStatus(),
            e.getIterations(),
            e.getTokenUsage(),
            e.getStartedAt(),
            e.getCompletedAt()
        );
    }
}
