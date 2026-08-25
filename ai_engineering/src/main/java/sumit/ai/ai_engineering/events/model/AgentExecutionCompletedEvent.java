package sumit.ai.ai_engineering.events.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgentExecutionCompletedEvent(
    UUID eventId,
    Long userId,
    UUID executionId,
    String goal,
    String status,
    int iterations,
    LocalDateTime timestamp
) implements BaseEvent {

    public static AgentExecutionCompletedEvent of(Long userId, UUID executionId, String goal, String status, int iterations) {
        return new AgentExecutionCompletedEvent(
            UUID.randomUUID(),
            userId,
            executionId,
            goal,
            status,
            iterations,
            LocalDateTime.now()
        );
    }

    @Override
    public String eventType() {
        return "agent.completed";
    }
}
