package sumit.ai.ai_engineering.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sumit.ai.ai_engineering.ai.tools.utility.CalculatorTool;
import sumit.ai.ai_engineering.ai.tools.utility.HashTool;
import sumit.ai.ai_engineering.mcp.model.McpServerInfo;
import sumit.ai.ai_engineering.mcp.model.McpToolCallRequest;
import sumit.ai.ai_engineering.mcp.model.McpToolCallResult;
import sumit.ai.ai_engineering.mcp.model.McpToolDefinition;
import sumit.ai.ai_engineering.mcp.server.McpServerServiceImpl;

class McpServerServiceTest {

    private McpServerServiceImpl mcpServer;

    @BeforeEach
    void setUp() {
        CalculatorTool calculator = new CalculatorTool();
        HashTool hashTool = new HashTool();
        mcpServer = new McpServerServiceImpl(List.of(calculator, hashTool));
    }

    @Test
    void getServerInfo_returnsCorrectMetadata() {
        McpServerInfo info = mcpServer.getServerInfo();
        assertThat(info.name()).isEqualTo("ai-engineering-mcp-server");
        assertThat(info.toolCount()).isGreaterThan(0);
    }

    @Test
    void listTools_exposesRegisteredToolDefinitions() {
        List<McpToolDefinition> tools = mcpServer.listTools();
        assertThat(tools).isNotEmpty();
        assertThat(tools.stream().anyMatch(t -> t.name().equalsIgnoreCase("add"))).isTrue();
    }

    @Test
    void executeTool_addTool_executesSuccessfully() {
        McpToolCallRequest req = new McpToolCallRequest("add", Map.of("a", 10.0, "b", 25.0));
        McpToolCallResult result = mcpServer.executeTool(req);

        assertThat(result.isError()).isFalse();
        assertThat(result.content()).isEqualTo(35.0);
    }

    @Test
    void executeTool_unknownTool_returnsErrorResult() {
        McpToolCallRequest req = new McpToolCallRequest("non_existent_tool", Map.of());
        McpToolCallResult result = mcpServer.executeTool(req);

        assertThat(result.isError()).isTrue();
        assertThat(result.errorMessage()).contains("Unknown tool");
    }
}
