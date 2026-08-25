package com.ai_engineering.ai_service.tools.utility;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.tools.AiTool;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonFormatter implements AiTool {
    
    private final ObjectMapper objectMapper;

    public JsonFormatter(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Pretty print a JSON string with indentation and line breaks.")
    public String prettyPrint(
        @ToolParam(description = "The JSON string to format.") String json
    ) throws JsonProcessingException{
        Object object = objectMapper.readValue(json, Object.class);
        return objectMapper
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(object);
    } 

    @Tool(description = "Minify a JSON string by removing unnecessary whitespace.")
    public String minify(
        @ToolParam(description = "The JSON string to compact.") String json
    ) throws JsonProcessingException{
        Object object = objectMapper.readValue(json, Object.class);
        return objectMapper.writeValueAsString(object);
    }

    @Tool(description = "Validate whether a string is valid JSON.")
    public boolean isValid(
        @ToolParam(description = "The JSON string to validate.") String json
    ) throws JsonProcessingException{
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ToolsCategory category() {
        return ToolsCategory.UTILITY;
    }
}
