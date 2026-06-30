package com.ai_engineering.ai_service.Service;


import com.ai_engineering.ai_service.DTOs.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.DTOs.ChatDTO.ChatResponse;

public interface AIService {
    ChatResponse chat(ChatRequest req);
}
