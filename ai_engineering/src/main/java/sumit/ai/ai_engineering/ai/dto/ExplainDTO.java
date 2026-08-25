package sumit.ai.ai_engineering.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class ExplainDTO {
    public record ExplainResponse(
        String explanation
    ){}
    public record ExplainRequest(
        String conversationId,
        @NotBlank(message = "No topic to explain")
        String topic
    ){}
}
