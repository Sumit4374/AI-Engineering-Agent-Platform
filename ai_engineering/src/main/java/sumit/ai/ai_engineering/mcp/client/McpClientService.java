package sumit.ai.ai_engineering.mcp.client;

import java.util.List;

import sumit.ai.ai_engineering.mcp.model.McpToolCallRequest;
import sumit.ai.ai_engineering.mcp.model.McpToolCallResult;
import sumit.ai.ai_engineering.mcp.model.McpToolDefinition;

public interface McpClientService {

    List<McpToolDefinition> discoverTools(String serverUrl);

    McpToolCallResult executeTool(String serverUrl, McpToolCallRequest request);
}
