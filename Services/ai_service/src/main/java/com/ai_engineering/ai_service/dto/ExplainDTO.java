package com.ai_engineering.ai_service.dto;

import com.ai_engineering.ai_service.capability.CapabilityRequest;

import jakarta.validation.constraints.NotBlank;

public class ExplainDTO {
    public record ExplainResponse(
        String explanation
    ){}
    public record ExplainRequest(
        String conversationId,
        @NotBlank(message = "No topic to explain")
        String topic
    ) implements CapabilityRequest {}
}
