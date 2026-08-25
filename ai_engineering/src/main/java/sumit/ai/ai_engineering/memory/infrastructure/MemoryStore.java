package sumit.ai.ai_engineering.memory.infrastructure;

import java.util.List;

import sumit.ai.ai_engineering.memory.model.MemoryMessage;

/**
 * Storage interface for short-lived conversational memory cache.
 */
public interface MemoryStore {

    List<MemoryMessage> getRecentMessages(String conversationId, int limit);

    void saveMessage(String conversationId, MemoryMessage message);

    void setMessages(String conversationId, List<MemoryMessage> messages);

    void clear(String conversationId);

    boolean exists(String conversationId);
}
