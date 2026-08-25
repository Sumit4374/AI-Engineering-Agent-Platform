package sumit.ai.ai_engineering.memory.model;

/**
 * Lightweight representation of a message within the AI memory context window.
 */
public record MemoryMessage(
    String role,
    String content,
    int estimatedTokens
) {
    public static MemoryMessage of(String role, String content) {
        int estimated = estimateTokens(content);
        return new MemoryMessage(role, content, estimated);
    }

    public static int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        // Heuristic: ~4 characters per token + 4 token overhead per message structure
        return Math.max(1, (text.length() / 4) + 4);
    }
}
