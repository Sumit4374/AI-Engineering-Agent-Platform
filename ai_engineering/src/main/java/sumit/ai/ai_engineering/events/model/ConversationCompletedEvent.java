package sumit.ai.ai_engineering.events.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationCompletedEvent(
    UUID eventId,
    Long userId,
    UUID conversationId,
    int messageCount,
    LocalDateTime timestamp
) implements BaseEvent {

    public static ConversationCompletedEvent of(Long userId, UUID conversationId, int messageCount) {
        return new ConversationCompletedEvent(
            UUID.randomUUID(),
            userId,
            conversationId,
            messageCount,
            LocalDateTime.now()
        );
    }

    @Override
    public String eventType() {
        return "conversation.completed";
    }
}
