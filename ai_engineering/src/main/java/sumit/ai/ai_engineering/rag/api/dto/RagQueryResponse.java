package sumit.ai.ai_engineering.rag.api.dto;

import java.util.List;

public record RagQueryResponse(
    String answer,
    String conversationId,
    List<RetrievedChunkDTO> sources,
    int totalSourcesFound
) {}
