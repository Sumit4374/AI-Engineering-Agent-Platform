package sumit.ai.ai_engineering.mcp.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import sumit.ai.ai_engineering.mcp.model.McpServerInfo;
import sumit.ai.ai_engineering.mcp.model.McpToolCallRequest;
import sumit.ai.ai_engineering.mcp.model.McpToolCallResult;
import sumit.ai.ai_engineering.mcp.model.McpToolDefinition;
import sumit.ai.ai_engineering.mcp.server.McpServerService;

@RestController
@RequestMapping("/api/v1/mcp")
public class McpController {

    private final McpServerService mcpServerService;

    public McpController(McpServerService mcpServerService) {
        this.mcpServerService = mcpServerService;
    }

    @GetMapping("/info")
    public ResponseEntity<McpServerInfo> getServerInfo() {
        return ResponseEntity.ok(mcpServerService.getServerInfo());
    }

    @GetMapping("/tools")
    public ResponseEntity<List<McpToolDefinition>> listTools() {
        return ResponseEntity.ok(mcpServerService.listTools());
    }

    @PostMapping("/execute")
    public ResponseEntity<McpToolCallResult> executeTool(@Valid @RequestBody McpToolCallRequest request) {
        McpToolCallResult result = mcpServerService.executeTool(request);
        return ResponseEntity.ok(result);
    }
}
