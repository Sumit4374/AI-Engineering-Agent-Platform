package com.ai_engineering.ai_service.dto;

import java.util.List;

import com.ai_engineering.ai_service.capability.CapabilityRequest;

import jakarta.validation.constraints.NotBlank;

public class RequirementDTO {

    public record RequirementRequest(
        String conversationId,
        @NotBlank(message = "Requirement text is required")
        String text
    ) implements CapabilityRequest {}

    public record RequirementAnalysisResponse(
        String summary,
        List<Requirement> functionalRequirements,
        List<Requirement> nonFunctionalRequirements,
        List<String> assumptions,
        List<String> openQuestions
    ) {}

    public record Requirement(
        String id,
        String description,
        String priority
    ) {}
}
