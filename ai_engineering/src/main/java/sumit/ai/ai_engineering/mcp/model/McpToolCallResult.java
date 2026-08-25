package sumit.ai.ai_engineering.mcp.model;

public record McpToolCallResult(
    String toolName,
    Object content,
    boolean isError,
    String errorMessage
) {
    public static McpToolCallResult success(String toolName, Object content) {
        return new McpToolCallResult(toolName, content, false, null);
    }

    public static McpToolCallResult error(String toolName, String errorMessage) {
        return new McpToolCallResult(toolName, null, true, errorMessage);
    }
}
