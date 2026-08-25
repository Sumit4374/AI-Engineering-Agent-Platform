package sumit.ai.ai_engineering.ai.dto;

import java.util.List;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import sumit.ai.ai_engineering.ai.prompt.SummaryType;

public class SummarizeDTO {
    public record SummarizeRequest(
        String conversationId,
        @NotBlank(message = "Content cannot be empty")
        String text,
        @NotNull(message = "Summary type is required")
        SummaryType type
    ){}

    public record SummarizeResponse(
        String title,
        String summary,
        List<String> keyPoints
    ){}
}
