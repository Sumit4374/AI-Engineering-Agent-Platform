package com.ai_engineering.ai_service.tools.utility;

import java.util.UUID;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.tools.AiTool;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class UuidTool implements AiTool {

    @Tool(description = "Generate a random UUID (version 4) and return it as a java.util.UUID.")
    public UUID generate() {
        return UUID.randomUUID();
    }

    @Override
    public ToolsCategory category() {
        return ToolsCategory.UTILITY;
    }
}
