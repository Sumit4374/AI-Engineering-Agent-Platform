package com.ai_engineering.ai_service.dto;

import java.util.List;

import com.ai_engineering.ai_service.model.enums.Complexity;

import jakarta.validation.constraints.NotBlank;

public class ReviewDTO {

    public record CodeReviewRequest(
        @NotBlank(message = "Code input is expected")
        String code
    ){}

    public record CodeReviewResponse(
        Complexity complexity,
        List<String> issues,
        List<String> recommendations
    ){}
}
