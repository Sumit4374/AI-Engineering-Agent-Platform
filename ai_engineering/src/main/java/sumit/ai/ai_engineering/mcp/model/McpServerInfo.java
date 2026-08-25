package sumit.ai.ai_engineering.mcp.model;

import java.util.List;

public record McpServerInfo(
    String name,
    String version,
    String protocolVersion,
    int toolCount,
    List<String> capabilities
) {}
