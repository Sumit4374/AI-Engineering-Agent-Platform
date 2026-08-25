package sumit.ai.ai_engineering.rag.model;

import java.util.UUID;

public record RetrievedChunk(
    UUID chunkId,
    UUID documentId,
    String content,
    double score,
    String fileName,
    String source,
    Integer page,
    String language
) {}
