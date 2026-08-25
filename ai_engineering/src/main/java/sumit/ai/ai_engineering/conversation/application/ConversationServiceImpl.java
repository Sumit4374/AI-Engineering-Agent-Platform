package sumit.ai.ai_engineering.conversation.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sumit.ai.ai_engineering.common.exception.ForbiddenAccessException;
import sumit.ai.ai_engineering.common.exception.ResourceNotFoundException;
import sumit.ai.ai_engineering.conversation.api.dto.ConversationDTO;
import sumit.ai.ai_engineering.conversation.api.dto.ConversationDetailDTO;
import sumit.ai.ai_engineering.conversation.api.dto.CreateConversationRequest;
import sumit.ai.ai_engineering.conversation.api.dto.MessageDTO;
import sumit.ai.ai_engineering.conversation.api.dto.UpdateConversationTitleRequest;
import sumit.ai.ai_engineering.conversation.domain.Conversation;
import sumit.ai.ai_engineering.conversation.domain.ConversationStatus;
import sumit.ai.ai_engineering.conversation.domain.Message;
import sumit.ai.ai_engineering.conversation.domain.MessageRole;
import sumit.ai.ai_engineering.conversation.infrastructure.ConversationRepository;
import sumit.ai.ai_engineering.conversation.infrastructure.MessageRepository;

@Service
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationServiceImpl.class);

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public ConversationDTO createConversation(Long userId, CreateConversationRequest request) {
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(request.title())
                .status(ConversationStatus.ACTIVE)
                .build();
        Conversation saved = conversationRepository.save(conversation);
        log.info("Created conversation [id={}, userId={}]", saved.getId(), userId);
        return ConversationDTO.from(saved);
    }

    @Override
    public Conversation getOrCreateConversation(Long userId, UUID conversationId, String defaultTitle) {
        if (conversationId != null) {
            return conversationRepository.findById(conversationId)
                    .map(conv -> {
                        if (!conv.getUserId().equals(userId)) {
                            throw new ForbiddenAccessException("Conversation does not belong to the user");
                        }
                        return conv;
                    })
                    .orElseGet(() -> {
                        Conversation newConv = Conversation.builder()
                                .id(conversationId)
                                .userId(userId)
                                .title(defaultTitle != null && !defaultTitle.isBlank() ? defaultTitle : "New Conversation")
                                .status(ConversationStatus.ACTIVE)
                                .build();
                        return conversationRepository.save(newConv);
                    });
        }
        Conversation newConv = Conversation.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(defaultTitle != null && !defaultTitle.isBlank() ? defaultTitle : "New Conversation")
                .status(ConversationStatus.ACTIVE)
                .build();
        return conversationRepository.save(newConv);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(ConversationDTO::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDetailDTO getConversation(Long userId, UUID conversationId) {
        Conversation conversation = findAndValidateOwnership(userId, conversationId);
        List<MessageDTO> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(MessageDTO::from)
                .toList();
        return ConversationDetailDTO.from(conversation, messages);
    }

    @Override
    public ConversationDTO updateTitle(Long userId, UUID conversationId, UpdateConversationTitleRequest request) {
        Conversation conversation = findAndValidateOwnership(userId, conversationId);
        conversation.setTitle(request.title());
        conversation.setUpdatedAt(LocalDateTime.now());
        Conversation updated = conversationRepository.save(conversation);
        return ConversationDTO.from(updated);
    }

    @Override
    public void deleteConversation(Long userId, UUID conversationId) {
        Conversation conversation = findAndValidateOwnership(userId, conversationId);
        messageRepository.deleteByConversationId(conversationId);
        conversationRepository.delete(conversation);
        log.info("Deleted conversation [id={}, userId={}]", conversationId, userId);
    }

    @Override
    public MessageDTO appendMessage(Long userId, UUID conversationId, MessageRole role, String content, Integer tokenUsage) {
        Conversation conversation = findAndValidateOwnership(userId, conversationId);
        
        Message message = Message.builder()
                .id(UUID.randomUUID())
                .conversationId(conversationId)
                .role(role)
                .content(content)
                .tokenUsage(tokenUsage)
                .build();
        Message saved = messageRepository.save(message);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        log.debug("Appended message [id={}, conversationId={}, role={}]", saved.getId(), conversationId, role);
        return MessageDTO.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(Long userId, UUID conversationId) {
        findAndValidateOwnership(userId, conversationId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(MessageDTO::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void validateOwnership(Long userId, UUID conversationId) {
        findAndValidateOwnership(userId, conversationId);
    }

    private Conversation findAndValidateOwnership(Long userId, UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        if (!conversation.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You do not have permission to access conversation: " + conversationId);
        }
        return conversation;
    }
}
