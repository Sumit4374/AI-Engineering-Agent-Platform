package com.ai_engineering.ai_service.dto;

import com.ai_engineering.ai_service.capability.CapabilityRequest;

import jakarta.validation.constraints.NotBlank;

public class ChatDTO {


    public record ChatRequest(
        String conversationId,
        String provider,
        @NotBlank(message = "Request should not be empty")
        String request
    ) implements CapabilityRequest {}
    public record ChatResponse(String response){}
}
