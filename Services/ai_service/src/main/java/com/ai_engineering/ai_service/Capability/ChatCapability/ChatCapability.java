package com.ai_engineering.ai_service.Capability.ChatCapability;

import java.io.IOException;


import com.ai_engineering.ai_service.dto.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.dto.ChatDTO.ChatResponse;

import reactor.core.publisher.Flux;

public interface ChatCapability {
    ChatResponse execute(ChatRequest request) throws IOException;
    Flux<String> stream(ChatRequest request) throws IOException;
}
