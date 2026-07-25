package com.ai_engineering.ai_service.engine;

import java.io.IOException;
import java.util.Map;

import com.ai_engineering.ai_service.tools.model.ToolsCategory;

public interface AIEngine {

    String generate(
        String conversationId,
        String provider,
        String promptType,
        Map<String, Object> variables,
        ToolsCategory... tools
    ) throws IOException;

    <T> T generateStructure(
        String conversationId,
        String provider,
        String promptType,
        Map<String, Object> variables,
        Class<T> responseType,
        ToolsCategory... tools
    ) throws IOException;

}
