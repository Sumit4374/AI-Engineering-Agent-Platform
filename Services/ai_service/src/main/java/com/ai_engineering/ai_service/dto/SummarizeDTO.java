package com.ai_engineering.ai_service.dto;

import java.util.List;

import com.ai_engineering.ai_service.capability.CapabilityRequest;
import com.ai_engineering.ai_service.prompt.SummaryType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SummarizeDTO {
    public record SummarizeRequest(
        String conversationId,
        String provider,
        @NotBlank(message = "Content cannot be empty")
        String text,
        @NotNull(message = "Summary type is required")
        SummaryType type
    ) implements CapabilityRequest {}

    public record SummarizeResponse(
        String title,
        String summary,
        List<String> keyPoints
    ){}
}
