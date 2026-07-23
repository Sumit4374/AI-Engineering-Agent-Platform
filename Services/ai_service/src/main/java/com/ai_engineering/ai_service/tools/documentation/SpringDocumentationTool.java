package com.ai_engineering.ai_service.tools.documentation;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.tools.AiTool;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class SpringDocumentationTool implements AiTool {

    @Tool(description = "Spring Documentation URL Generator(topic) - Returns the URL for the Spring documentation based on the provided topic")
    public String documentationUrl(
        @ToolParam(description = "The topic for which you want to retrieve the Spring documentation URL")
        String topic
    ){
        return switch(topic.toLowerCase()){
            case "security" -> "https://docs.spring.io/spring-security/reference/";
            case "data jpa" -> "https://docs.spring.io/spring-data/jpa/reference/";
            default -> "https://docs.spring.io/";
        };
    }

    @Override
    public ToolsCategory category() {
        return ToolsCategory.DOCUMENTATION;
    }
}
