package sumit.ai.ai_engineering.memory.model;

import java.util.List;

/**
 * Bounded conversation context prepared for inclusion in LLM prompt.
 */
public record ConversationContext(
    String conversationId,
    List<MemoryMessage> messages,
    int totalTokens,
    String summary
) {
    public static ConversationContext empty(String conversationId) {
        return new ConversationContext(conversationId, List.of(), 0, null);
    }
}
