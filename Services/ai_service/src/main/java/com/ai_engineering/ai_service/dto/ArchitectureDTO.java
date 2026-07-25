package com.ai_engineering.ai_service.dto;

import java.util.List;

import com.ai_engineering.ai_service.capability.CapabilityRequest;

import jakarta.validation.constraints.NotBlank;

public class ArchitectureDTO {

    public record ArchitectureRequest(
        String conversationId,
        String provider,
        @NotBlank(message = "Architecture description or code is required")
        String context
    ) implements CapabilityRequest {}

    public record ArchitectureReviewResponse(
        String summary,
        List<ArchitectureComponent> components,
        List<String> strengths,
        List<ArchitectureConcern> concerns,
        List<String> recommendations
    ) {}

    public record ArchitectureComponent(
        String name,
        String responsibility
    ) {}

    public record ArchitectureConcern(
        String area,
        String description,
        String impact
    ) {}
}
