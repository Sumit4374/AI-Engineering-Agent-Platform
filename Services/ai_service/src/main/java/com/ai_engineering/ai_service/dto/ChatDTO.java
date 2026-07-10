package com.ai_engineering.ai_service.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatDTO {

    
    public record ChatRequest(
        @NotBlank(message = "Request should not be empty")
        String request
    ){}
    public record ChatResponse(String response){}
}
