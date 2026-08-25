package sumit.ai.ai_engineering.conversation.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import sumit.ai.ai_engineering.conversation.domain.Message;
import sumit.ai.ai_engineering.conversation.domain.MessageRole;

public record MessageDTO(
    UUID id,
    UUID conversationId,
    MessageRole role,
    String content,
    Integer tokenUsage,
    LocalDateTime createdAt
) {
    public static MessageDTO from(Message m) {
        return new MessageDTO(
            m.getId(),
            m.getConversationId(),
            m.getRole(),
            m.getContent(),
            m.getTokenUsage(),
            m.getCreatedAt()
        );
    }
}
