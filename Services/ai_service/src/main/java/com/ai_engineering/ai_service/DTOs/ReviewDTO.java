package com.ai_engineering.ai_service.DTOs;

import java.util.List;

import com.ai_engineering.ai_service.Model.Enums.Complexity;

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
