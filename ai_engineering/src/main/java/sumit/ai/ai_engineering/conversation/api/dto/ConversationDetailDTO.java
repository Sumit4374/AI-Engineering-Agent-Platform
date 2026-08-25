package sumit.ai.ai_engineering.conversation.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import sumit.ai.ai_engineering.conversation.domain.Conversation;
import sumit.ai.ai_engineering.conversation.domain.ConversationStatus;

public record ConversationDetailDTO(
    UUID id,
    Long userId,
    String title,
    ConversationStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<MessageDTO> messages
) {
    public static ConversationDetailDTO from(Conversation c, List<MessageDTO> messages) {
        return new ConversationDetailDTO(
            c.getId(),
            c.getUserId(),
            c.getTitle(),
            c.getStatus(),
            c.getCreatedAt(),
            c.getUpdatedAt(),
            messages
        );
    }
}
