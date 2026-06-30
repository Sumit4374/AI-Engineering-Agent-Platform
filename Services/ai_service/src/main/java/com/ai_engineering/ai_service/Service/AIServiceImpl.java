package com.ai_engineering.ai_service.Service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.ai_engineering.ai_service.DTOs.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.DTOs.ChatDTO.ChatResponse;

@Service
public class AIServiceImpl implements AIService {

    private final  ChatClient chatClient;

    AIServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public ChatResponse chat(ChatRequest req) {
        return new ChatResponse(
            chatClient
                .prompt(req.request())
                .system("You are a senior java Engineer")
                .call()
                .content()
        );
    }
    
}
