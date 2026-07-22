package com.ai_engineering.ai_service.tools.development;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class RegexGeneratorTool {
    
    @Tool(description = "Generate a regular expression to validate standard email addresses (local-part@domain)")
    public String emailRegex(){
        return "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    }

    @Tool(description = "Generate a regular expression to validate 10-digit phone numbers (digits only, no leading zero)")
    public String phoneRegex(){
        return "^[1-9]{10}$";
    }
}
