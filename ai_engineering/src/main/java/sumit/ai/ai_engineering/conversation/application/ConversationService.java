package sumit.ai.ai_engineering.conversation.application;

import java.util.List;
import java.util.UUID;

import sumit.ai.ai_engineering.conversation.api.dto.ConversationDTO;
import sumit.ai.ai_engineering.conversation.api.dto.ConversationDetailDTO;
import sumit.ai.ai_engineering.conversation.api.dto.CreateConversationRequest;
import sumit.ai.ai_engineering.conversation.api.dto.MessageDTO;
import sumit.ai.ai_engineering.conversation.api.dto.UpdateConversationTitleRequest;
import sumit.ai.ai_engineering.conversation.domain.Conversation;
import sumit.ai.ai_engineering.conversation.domain.MessageRole;

public interface ConversationService {

    ConversationDTO createConversation(Long userId, CreateConversationRequest request);

    Conversation getOrCreateConversation(Long userId, UUID conversationId, String defaultTitle);

    List<ConversationDTO> getUserConversations(Long userId);

    ConversationDetailDTO getConversation(Long userId, UUID conversationId);

    ConversationDTO updateTitle(Long userId, UUID conversationId, UpdateConversationTitleRequest request);

    void deleteConversation(Long userId, UUID conversationId);

    MessageDTO appendMessage(Long userId, UUID conversationId, MessageRole role, String content, Integer tokenUsage);

    List<MessageDTO> getMessages(Long userId, UUID conversationId);

    void validateOwnership(Long userId, UUID conversationId);
}
