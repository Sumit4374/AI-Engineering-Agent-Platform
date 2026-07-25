package com.ai_engineering.ai_service.dto;

import java.util.List;

import com.ai_engineering.ai_service.capability.CapabilityRequest;

import jakarta.validation.constraints.NotBlank;

public class DebugDTO {

    public record DebugRequest(
        String conversationId,
        String provider,
        @NotBlank(message = "Code or error context is required")
        String code,
        String stackTrace
    ) implements CapabilityRequest {}

    public record DebugResponse(
        String rootCause,
        List<String> likelyCauses,
        List<DebugStep> suggestedFixes,
        String explanation
    ) {}

    public record DebugStep(
        String action,
        String detail
    ) {}
}
