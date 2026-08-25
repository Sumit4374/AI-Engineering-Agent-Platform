package sumit.ai.ai_engineering.rag.retrieval;

import java.io.IOException;
import java.util.List;

import sumit.ai.ai_engineering.rag.model.RagAnswer;
import sumit.ai.ai_engineering.rag.model.RetrievedChunk;

public interface RagRetrievalService {

    List<RetrievedChunk> retrieve(Long userId, String query, int topK, double minSimilarity);

    String formatContext(List<RetrievedChunk> chunks);

    RagAnswer query(Long userId, String conversationId, String query, int topK, double minSimilarity) throws IOException;
}
