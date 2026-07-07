package com.ai_engineering.ai_service.Engine;

import java.io.IOException;
import java.util.Map;

import com.ai_engineering.ai_service.Engine.Prompts.PromptType;

public interface AIEngine {
    <T> T generate(
        PromptType promptType,
        Map<String, Object> variables,
        Class<T> responseType
    ) throws IOException;
}
