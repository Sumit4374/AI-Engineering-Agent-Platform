package sumit.ai.ai_engineering.rag.vector;

import java.util.List;
import java.util.UUID;

import sumit.ai.ai_engineering.rag.model.DocumentChunk;
import sumit.ai.ai_engineering.rag.model.RetrievedChunk;

public interface VectorStoreService {

    void storeChunks(List<DocumentChunk> chunks);

    List<RetrievedChunk> searchSimilar(Long userId, float[] queryEmbedding, int topK, double minSimilarity);

    void deleteByDocumentId(UUID documentId);

    void deleteByUserId(Long userId);
}
