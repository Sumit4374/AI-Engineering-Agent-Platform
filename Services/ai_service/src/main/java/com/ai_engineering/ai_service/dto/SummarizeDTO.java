package com.ai_engineering.ai_service.dto;

import java.util.List;

import com.ai_engineering.ai_service.prompt.SummaryType;

import jakarta.validation.constraints.NotBlank;

public class SummarizeDTO {
    public record SummarizeRequest(
        @NotBlank(message = "Content cannot be empty")
        String text,
        SummaryType type
    ){}

    public record SummarizeResponse(
        String title,
        String summary,
        List<String> keyPoints
    ){}
}
