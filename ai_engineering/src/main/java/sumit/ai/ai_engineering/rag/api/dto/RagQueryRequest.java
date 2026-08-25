package sumit.ai.ai_engineering.rag.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RagQueryRequest(
    String conversationId,

    @NotBlank(message = "Query must not be blank")
    String query,

    Integer topK,

    Double minSimilarity
) {}
