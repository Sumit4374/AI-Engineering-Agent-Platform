package com.ai_engineering.ai_service.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai_engineering.ai_service.DTOs.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.DTOs.ChatDTO.ChatResponse;
import com.ai_engineering.ai_service.Service.AIService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ai")
public class AIController {
    private final AIService aiService;

    public AIController(AIService aiService){
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> getChatResponse(@Valid @RequestBody ChatRequest request){
        return ResponseEntity.ok(aiService.chat(request));
    }
}
