package sumit.ai.ai_engineering.memory.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import sumit.ai.ai_engineering.conversation.domain.Message;
import sumit.ai.ai_engineering.conversation.infrastructure.MessageRepository;
import sumit.ai.ai_engineering.memory.infrastructure.MemoryStore;
import sumit.ai.ai_engineering.memory.model.ConversationContext;
import sumit.ai.ai_engineering.memory.model.MemoryMessage;

@Service
public class ConversationMemoryServiceImpl implements ConversationMemoryService {

    private static final Logger log = LoggerFactory.getLogger(ConversationMemoryServiceImpl.class);

    private final MemoryStore memoryStore;
    private final MessageRepository messageRepository;

    public ConversationMemoryServiceImpl(MemoryStore memoryStore, MessageRepository messageRepository) {
        this.memoryStore = memoryStore;
        this.messageRepository = messageRepository;
    }

    @Override
    public ConversationContext getBoundedContext(String conversationId, int maxTokens, int maxMessages) {
        if (conversationId == null || conversationId.isBlank()) {
            return ConversationContext.empty("");
        }

        List<MemoryMessage> messages = getOrHydrateMessages(conversationId);
        if (messages.isEmpty()) {
            return ConversationContext.empty(conversationId);
        }

        // Bounded context selection: take from most recent backwards
        List<MemoryMessage> bounded = new ArrayList<>();
        int accumulatedTokens = 0;

        for (int i = messages.size() - 1; i >= 0; i--) {
            MemoryMessage msg = messages.get(i);
            if (bounded.size() >= maxMessages) {
                break;
            }
            if (accumulatedTokens + msg.estimatedTokens() > maxTokens && !bounded.isEmpty()) {
                break;
            }
            bounded.add(msg);
            accumulatedTokens += msg.estimatedTokens();
        }

        // Restore chronological order
        Collections.reverse(bounded);

        log.debug("Bounded context for [conversationId={}: {} messages, ~{} tokens]",
                conversationId, bounded.size(), accumulatedTokens);

        return new ConversationContext(conversationId, bounded, accumulatedTokens, null);
    }

    @Override
    public void recordMessage(String conversationId, String role, String content) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }
        MemoryMessage message = MemoryMessage.of(role, content);
        memoryStore.saveMessage(conversationId, message);
    }

    @Override
    public void clearMemory(String conversationId) {
        if (conversationId != null && !conversationId.isBlank()) {
            memoryStore.clear(conversationId);
        }
    }

    private List<MemoryMessage> getOrHydrateMessages(String conversationId) {
        if (memoryStore.exists(conversationId)) {
            return memoryStore.getRecentMessages(conversationId, 50);
        }

        // Cold start hydration from PostgreSQL
        try {
            UUID convUuid = UUID.fromString(conversationId);
            List<Message> persisted = messageRepository.findByConversationIdOrderByCreatedAtAsc(convUuid);
            if (!persisted.isEmpty()) {
                List<MemoryMessage> hydrated = persisted.stream()
                        .map(m -> new MemoryMessage(
                                m.getRole().name(),
                                m.getContent(),
                                m.getTokenUsage() != null ? m.getTokenUsage() : MemoryMessage.estimateTokens(m.getContent())
                        ))
                        .toList();
                memoryStore.setMessages(conversationId, hydrated);
                log.debug("Hydrated memory store from database for conversation [id={}, count={}]",
                        conversationId, hydrated.size());
                return hydrated;
            }
        } catch (IllegalArgumentException ignored) {
            // Not a UUID format (e.g. temporary/mock session ID)
        }

        return List.of();
    }
}
