package sumit.ai.ai_engineering.mcp.model;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

public record McpToolCallRequest(
    @NotBlank(message = "Tool name must not be blank")
    String toolName,

    Map<String, Object> arguments
) {}
