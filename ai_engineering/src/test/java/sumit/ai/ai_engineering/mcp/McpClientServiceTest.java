package sumit.ai.ai_engineering.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sumit.ai.ai_engineering.mcp.client.McpClientServiceImpl;
import sumit.ai.ai_engineering.mcp.model.McpToolCallRequest;
import sumit.ai.ai_engineering.mcp.model.McpToolCallResult;
import sumit.ai.ai_engineering.mcp.model.McpToolDefinition;
import sumit.ai.ai_engineering.mcp.server.McpServerService;

@ExtendWith(MockitoExtension.class)
class McpClientServiceTest {

    @Mock private McpServerService localMcpServer;

    private McpClientServiceImpl client;

    @BeforeEach
    void setUp() {
        client = new McpClientServiceImpl(localMcpServer);
    }

    @Test
    void discoverTools_localUrl_delegatesToLocalServer() {
        when(localMcpServer.listTools()).thenReturn(List.of(
                new McpToolDefinition("readFile", "Read file", "FILESYSTEM", Map.of())
        ));

        List<McpToolDefinition> tools = client.discoverTools("local");

        assertThat(tools).hasSize(1);
        assertThat(tools.get(0).name()).isEqualTo("readFile");
        verify(localMcpServer).listTools();
    }

    @Test
    void executeTool_localUrl_delegatesToLocalServer() {
        McpToolCallRequest req = new McpToolCallRequest("readFile", Map.of("relativePath", "pom.xml"));
        McpToolCallResult expected = McpToolCallResult.success("readFile", "<xml>...</xml>");

        when(localMcpServer.executeTool(req)).thenReturn(expected);

        McpToolCallResult result = client.executeTool(null, req);

        assertThat(result.isError()).isFalse();
        assertThat(result.content()).isEqualTo("<xml>...</xml>");
        verify(localMcpServer).executeTool(req);
    }
}
