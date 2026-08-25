package sumit.ai.ai_engineering.conversation.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import sumit.ai.ai_engineering.conversation.domain.Conversation;
import sumit.ai.ai_engineering.conversation.domain.ConversationStatus;

public record ConversationDTO(
    UUID id,
    Long userId,
    String title,
    ConversationStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ConversationDTO from(Conversation c) {
        return new ConversationDTO(
            c.getId(),
            c.getUserId(),
            c.getTitle(),
            c.getStatus(),
            c.getCreatedAt(),
            c.getUpdatedAt()
        );
    }
}
