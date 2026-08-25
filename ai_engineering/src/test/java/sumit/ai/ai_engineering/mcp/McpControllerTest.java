package sumit.ai.ai_engineering.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import sumit.ai.ai_engineering.mcp.api.McpController;
import sumit.ai.ai_engineering.mcp.model.McpServerInfo;
import sumit.ai.ai_engineering.mcp.model.McpToolCallRequest;
import sumit.ai.ai_engineering.mcp.model.McpToolCallResult;
import sumit.ai.ai_engineering.mcp.model.McpToolDefinition;
import sumit.ai.ai_engineering.mcp.server.McpServerService;

@ExtendWith(MockitoExtension.class)
class McpControllerTest {

    @Mock private McpServerService mcpServerService;

    private McpController controller;

    @BeforeEach
    void setUp() {
        controller = new McpController(mcpServerService);
    }

    @Test
    void getServerInfo_returnsOkWithInfo() {
        McpServerInfo info = new McpServerInfo("server", "1.0", "2024-11-05", 10, List.of("tools"));
        when(mcpServerService.getServerInfo()).thenReturn(info);

        ResponseEntity<McpServerInfo> resp = controller.getServerInfo();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().name()).isEqualTo("server");
    }

    @Test
    void listTools_returnsToolsList() {
        when(mcpServerService.listTools()).thenReturn(List.of(
                new McpToolDefinition("add", "Add numbers", "UTILITY", Map.of())
        ));

        ResponseEntity<List<McpToolDefinition>> resp = controller.listTools();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    void executeTool_returnsExecutionResult() {
        McpToolCallRequest req = new McpToolCallRequest("add", Map.of("a", 2, "b", 3));
        McpToolCallResult result = McpToolCallResult.success("add", 5.0);

        when(mcpServerService.executeTool(req)).thenReturn(result);

        ResponseEntity<McpToolCallResult> resp = controller.executeTool(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().content()).isEqualTo(5.0);
    }
}
