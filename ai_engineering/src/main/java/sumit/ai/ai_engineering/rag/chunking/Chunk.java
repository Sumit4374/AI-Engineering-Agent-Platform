package sumit.ai.ai_engineering.rag.chunking;

import java.util.UUID;

import sumit.ai.ai_engineering.rag.model.ChunkMetadata;

public record Chunk(
    int index,
    String content,
    int estimatedTokens,
    ChunkMetadata metadata
) {}
