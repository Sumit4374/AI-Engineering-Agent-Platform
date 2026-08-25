package sumit.ai.ai_engineering.agent.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import sumit.ai.ai_engineering.agent.model.AgentExecution;
import sumit.ai.ai_engineering.agent.model.AgentStatus;

public record AgentExecutionDetailDTO(
    UUID id,
    Long userId,
    String goal,
    AgentStatus status,
    int iterations,
    int tokenUsage,
    String planJson,
    String result,
    String error,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    List<AgentStepDTO> steps
) {
    public static AgentExecutionDetailDTO from(AgentExecution e, List<AgentStepDTO> steps) {
        return new AgentExecutionDetailDTO(
            e.getId(),
            e.getUserId(),
            e.getGoal(),
            e.getStatus(),
            e.getIterations(),
            e.getTokenUsage(),
            e.getPlanJson(),
            e.getResult(),
            e.getError(),
            e.getStartedAt(),
            e.getCompletedAt(),
            steps != null ? steps : List.of()
        );
    }
}
