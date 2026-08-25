package sumit.ai.ai_engineering.mcp.model;

import java.util.Map;

public record McpToolDefinition(
    String name,
    String description,
    String category,
    Map<String, Object> inputSchema
) {}
