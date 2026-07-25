package com.ai_engineering.ai_service.dto;

import com.ai_engineering.ai_service.capability.CapabilityRequest;

import jakarta.validation.constraints.NotBlank;

public class ApiExplanationDTO {

    public record ApiExplanationRequest(
        String conversationId,
        String provider,
        @NotBlank(message = "API definition or code is required")
        String api
    ) implements CapabilityRequest {}

    public record ApiExplanationResponse(
        String explanation
    ) {}
}
