package com.ai_engineering.ai_service.tools.utility;

import java.util.UUID;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class UuidTool {
    
    @Tool(description = "Generate a random UUID (version 4) and return it as a java.util.UUID.")
    public UUID generate() {
        return UUID.randomUUID();
    }
}
