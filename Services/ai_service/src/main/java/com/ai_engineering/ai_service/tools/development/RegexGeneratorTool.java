package com.ai_engineering.ai_service.tools.development;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.tools.AiTool;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class RegexGeneratorTool implements AiTool {

    @Tool(description = "Generate a regular expression to validate standard email addresses (local-part@domain)")
    public String emailRegex(){
        return "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    }

    @Tool(description = "Generate a regular expression to validate 10-digit phone numbers (digits only, no leading zero)")
    public String phoneRegex(){
        return "^[1-9]{10}$";
    }

    @Override
    public ToolsCategory category() {
        return ToolsCategory.DEVELOPMENT;
    }
}
