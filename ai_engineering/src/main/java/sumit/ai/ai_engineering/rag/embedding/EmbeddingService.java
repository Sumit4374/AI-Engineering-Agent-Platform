package sumit.ai.ai_engineering.rag.embedding;

import java.util.List;

/**
 * Provider-independent embedding service abstraction.
 */
public interface EmbeddingService {

    float[] embed(String text);

    List<float[]> embedBatch(List<String> texts);

    int getDimensions();
}
