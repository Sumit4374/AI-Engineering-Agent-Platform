package com.ai_engineering.ai_service.dto;

import jakarta.validation.constraints.NotBlank;

public class ExplainDTO {
    public record ExplainResponse(
        String explaination
    ){}
    public record ExplainRequest(
        @NotBlank(message = "No topic to explain")
        String topic
    ){}
}
