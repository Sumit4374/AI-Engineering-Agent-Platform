package com.ai_engineering.ai_service.Capability.ChatCapability;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ai_engineering.ai_service.dto.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.dto.ChatDTO.ChatResponse;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.PromptType;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

import reactor.core.publisher.Flux;

@Service
public class ChatCapabilityImpl implements ChatCapability {

    private final AIEngine engine;

    ChatCapabilityImpl(AIEngine engine){
        this.engine = engine;
    }

    @Override
    public ChatResponse execute(ChatRequest request) throws IOException {
        return new ChatResponse(
            engine.generate(request.conversationId(),
            PromptType.CHAT.getFileName(),
            Map.of("question",request.request()),
            ToolsCategory.UTILITY)
        );
    }

    @Override
    public Flux<String> stream(ChatRequest request) throws IOException{
        return engine.stream(
            request.conversationId(),
            PromptType.CHAT.getFileName(),
            Map.of(
                "question",
                request.request()
            ), 
            ToolsCategory.UTILITY
        );
    }
    
}
