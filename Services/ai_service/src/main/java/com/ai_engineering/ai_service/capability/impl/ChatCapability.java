package com.ai_engineering.ai_service.capability.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.capability.AbstractTextCapability;
import com.ai_engineering.ai_service.capability.CapabilityType;
import com.ai_engineering.ai_service.dto.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.dto.ChatDTO.ChatResponse;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.PromptType;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class ChatCapability extends AbstractTextCapability<ChatRequest, ChatResponse> {

    public ChatCapability(AIEngine engine) {
        super(engine);
    }

    @Override
    public CapabilityType type() {
        return CapabilityType.CHAT;
    }

    @Override
    protected String promptType() {
        return PromptType.CHAT.getFileName();
    }

    @Override
    protected ToolsCategory[] tools() {
        return new ToolsCategory[]{
            ToolsCategory.UTILITY, ToolsCategory.DEVELOPMENT, ToolsCategory.DOCUMENTATION
        };
    }

    @Override
    protected Map<String, Object> variables(ChatRequest request) {
        return Map.of("question", request.request());
    }

    @Override
    protected ChatResponse map(String content) {
        return new ChatResponse(content);
    }
}
