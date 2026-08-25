package sumit.ai.ai_engineering.mcp.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import sumit.ai.ai_engineering.mcp.model.McpToolCallRequest;
import sumit.ai.ai_engineering.mcp.model.McpToolCallResult;
import sumit.ai.ai_engineering.mcp.model.McpToolDefinition;
import sumit.ai.ai_engineering.mcp.server.McpServerService;

@Service
public class McpClientServiceImpl implements McpClientService {

    private static final Logger log = LoggerFactory.getLogger(McpClientServiceImpl.class);

    private final McpServerService localMcpServer;
    private final RestClient restClient;

    public McpClientServiceImpl(McpServerService localMcpServer) {
        this.localMcpServer = localMcpServer;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public List<McpToolDefinition> discoverTools(String serverUrl) {
        if (serverUrl == null || serverUrl.isBlank() || "local".equalsIgnoreCase(serverUrl)) {
            return localMcpServer.listTools();
        }

        try {
            log.debug("Discovering MCP tools from remote endpoint: [{}]", serverUrl);
            @SuppressWarnings("unchecked")
            List<McpToolDefinition> tools = restClient.get()
                    .uri(serverUrl + "/api/v1/mcp/tools")
                    .retrieve()
                    .body(List.class);
            return tools != null ? tools : List.of();
        } catch (Exception e) {
            log.warn("Remote MCP tool discovery failed for [{}]: {}", serverUrl, e.getMessage());
            return List.of();
        }
    }

    @Override
    public McpToolCallResult executeTool(String serverUrl, McpToolCallRequest request) {
        if (serverUrl == null || serverUrl.isBlank() || "local".equalsIgnoreCase(serverUrl)) {
            return localMcpServer.executeTool(request);
        }

        try {
            log.debug("Executing MCP tool [{}] on remote endpoint [{}]", request.toolName(), serverUrl);
            return restClient.post()
                    .uri(serverUrl + "/api/v1/mcp/execute")
                    .body(request)
                    .retrieve()
                    .body(McpToolCallResult.class);
        } catch (Exception e) {
            log.error("Remote MCP tool execution failed on [{}]: {}", serverUrl, e.getMessage());
            return McpToolCallResult.error(request.toolName(), "Remote tool execution failed: " + e.getMessage());
        }
    }
}
