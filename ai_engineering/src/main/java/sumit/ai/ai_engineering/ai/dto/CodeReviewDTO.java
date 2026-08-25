package com.ai_engineering.ai_service.dto;

import java.util.List;


import jakarta.validation.constraints.NotBlank;

public class CodeReviewDTO {

    public record CodeReviewRequest(
        String conversationId,
        @NotBlank(message = "Code input is expected")
        String code
    ){}

    public record CodeReviewResponse(
        ReviewScore score,
        String overallAssessment,
        List<ReviewIssues> issues,
        List<Recommendation> recommendation
    ){}

    public record ReviewScore(
        int maintainability,
        int readability,
        int performance,
        int security
    ){}

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    public record ReviewIssues(
        Severity severity,
        String title,
        String explanation,
        String location
    ) {}

    public record Recommendation(
        String title,
        String reason
    ) {}
}
