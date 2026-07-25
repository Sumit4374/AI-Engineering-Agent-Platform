package com.ai_engineering.ai_service.dto;

import java.util.List;

import com.ai_engineering.ai_service.capability.CapabilityRequest;

import jakarta.validation.constraints.NotBlank;

public class DocumentationDTO {

    public record DocumentationRequest(
        String conversationId,
        String provider,
        @NotBlank(message = "Code to document is required")
        String code
    ) implements CapabilityRequest {}

    public record DocumentationResponse(
        String title,
        String overview,
        List<DocumentedElement> elements,
        String usageExample
    ) {}

    public record DocumentedElement(
        String name,
        String kind,
        String description
    ) {}
}
