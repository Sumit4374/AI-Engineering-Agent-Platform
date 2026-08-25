package sumit.ai.ai_engineering.rag.model;

import java.util.List;

public record RagAnswer(
    String answer,
    String conversationId,
    List<RetrievedChunk> sources,
    int totalSourcesFound
) {}
