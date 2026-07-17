package com.ai_engineering.ai_service.engine;

import java.io.IOException;
import java.util.Map;


public interface AIEngine {

    String generate(
        String conversationId,
        String promptType,
        Map<String, Object> variables
    ) throws IOException;

    <T> T generateStructure(
        String conversationId,
        String promptType,
        Map<String, Object> variables,
        Class<T> responseType
    ) throws IOException;
    
}
