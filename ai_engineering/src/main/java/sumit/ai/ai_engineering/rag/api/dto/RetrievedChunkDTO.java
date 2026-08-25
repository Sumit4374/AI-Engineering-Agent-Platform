package sumit.ai.ai_engineering.rag.api.dto;

import java.util.UUID;

import sumit.ai.ai_engineering.rag.model.RetrievedChunk;

public record RetrievedChunkDTO(
    UUID chunkId,
    UUID documentId,
    String content,
    double score,
    String fileName,
    Integer page,
    String language
) {
    public static RetrievedChunkDTO from(RetrievedChunk c) {
        return new RetrievedChunkDTO(
            c.chunkId(),
            c.documentId(),
            c.content(),
            c.score(),
            c.fileName(),
            c.page(),
            c.language()
        );
    }
}
