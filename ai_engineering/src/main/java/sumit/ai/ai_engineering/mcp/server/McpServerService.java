package sumit.ai.ai_engineering.mcp.server;

import java.util.List;

import sumit.ai.ai_engineering.mcp.model.McpServerInfo;
import sumit.ai.ai_engineering.mcp.model.McpToolCallRequest;
import sumit.ai.ai_engineering.mcp.model.McpToolCallResult;
import sumit.ai.ai_engineering.mcp.model.McpToolDefinition;

public interface McpServerService {

    McpServerInfo getServerInfo();

    List<McpToolDefinition> listTools();

    McpToolCallResult executeTool(McpToolCallRequest request);
}
