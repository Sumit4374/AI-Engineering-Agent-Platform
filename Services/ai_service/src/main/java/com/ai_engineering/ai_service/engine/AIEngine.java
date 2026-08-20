package com.ai_engineering.ai_service.engine;

import java.io.IOException;
import java.util.Map;

import com.ai_engineering.ai_service.tools.model.ToolsCategory;

import reactor.core.publisher.Flux;

public interface AIEngine {

    String generate(
        String conversationId,
        String promptType,
        Map<String, Object> variables,
        ToolsCategory... tools
    ) throws IOException;

    <T> T generateStructure(
        String conversationId,
        String promptType,
        Map<String, Object> variables,
        Class<T> responseType,
        ToolsCategory... tools
    ) throws IOException;

    Flux<String> stream(
        String conversationID,
        String promptType,
        Map<String,Object> variables,
        ToolsCategory... tools
    ) throws IOException;

}
