package com.ai_engineering.ai_service.engine;

import java.io.IOException;
import java.util.Map;

import com.ai_engineering.ai_service.prompt.PromptType;

public interface AIEngine {

    String generate(
        PromptType promptType,
        Map<String, Object> variables
    ) throws IOException;

    <T> T generateStructure(
        PromptType promptType,
        Map<String, Object> variables,
        Class<T> responseType
    ) throws IOException;
    
}
