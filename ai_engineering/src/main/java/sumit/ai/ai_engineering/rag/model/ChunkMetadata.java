package sumit.ai.ai_engineering.rag.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChunkMetadata(
    UUID documentId,
    UUID chunkId,
    String source,
    String fileName,
    Integer page,
    String language,
    LocalDateTime createdAt
) {}
