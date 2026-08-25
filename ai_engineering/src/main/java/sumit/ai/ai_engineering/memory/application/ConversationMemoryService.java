package sumit.ai.ai_engineering.memory.application;

import sumit.ai.ai_engineering.memory.model.ConversationContext;

/**
 * Service managing bounded context window retrieval and conversational state for LLM calls.
 */
public interface ConversationMemoryService {

    /**
     * Retrieves bounded recent context for the given conversation ID.
     * Enforces token budget and message count limits.
     * Hydrates from PostgreSQL on cold start cache miss.
     */
    ConversationContext getBoundedContext(String conversationId, int maxTokens, int maxMessages);

    /**
     * Appends a message to memory state.
     */
    void recordMessage(String conversationId, String role, String content);

    /**
     * Clears memory cache for a conversation.
     */
    void clearMemory(String conversationId);
}
