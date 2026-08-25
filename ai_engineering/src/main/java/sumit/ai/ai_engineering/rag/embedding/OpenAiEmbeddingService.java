package sumit.ai.ai_engineering.rag.embedding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class OpenAiEmbeddingService implements EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiEmbeddingService.class);
    private static final int DEFAULT_DIMENSIONS = 1536;

    private final ObjectProvider<OpenAiEmbeddingModel> embeddingModelProvider;
    private final Map<String, float[]> embeddingCache = new ConcurrentHashMap<>();

    public OpenAiEmbeddingService(ObjectProvider<OpenAiEmbeddingModel> embeddingModelProvider) {
        this.embeddingModelProvider = embeddingModelProvider;
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new float[DEFAULT_DIMENSIONS];
        }

        String cacheKey = hashText(text);
        float[] cached = embeddingCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        OpenAiEmbeddingModel model = embeddingModelProvider.getIfAvailable();
        if (model != null) {
            try {
                float[] vector = model.embed(text);
                if (vector != null && vector.length > 0) {
                    embeddingCache.put(cacheKey, vector);
                    return vector;
                }
            } catch (Exception e) {
                log.warn("Remote embedding call failed, falling back to deterministic local embedding: {}", e.getMessage());
            }
        }

        // Deterministic local feature embedding fallback
        float[] fallbackVector = generateDeterministicEmbedding(text, DEFAULT_DIMENSIONS);
        embeddingCache.put(cacheKey, fallbackVector);
        return fallbackVector;
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        List<float[]> results = new ArrayList<>(texts.size());
        for (String text : texts) {
            results.add(embed(text));
        }
        return results;
    }

    @Override
    public int getDimensions() {
        return DEFAULT_DIMENSIONS;
    }

    public static float[] generateDeterministicEmbedding(String text, int dimensions) {
        float[] vector = new float[dimensions];
        if (text == null || text.isBlank()) {
            return vector;
        }

        String[] tokens = text.toLowerCase().split("\\W+");
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            int hash = Math.abs(token.hashCode());
            int idx = hash % dimensions;
            vector[idx] += 1.0f;
            // Also spread hash across secondary dimensions for semantic richness
            int idx2 = (hash * 31 + 17) % dimensions;
            vector[Math.abs(idx2)] += 0.5f;
        }

        // L2 Normalize the vector
        double sumSq = 0.0;
        for (float v : vector) {
            sumSq += v * v;
        }
        if (sumSq > 0) {
            double norm = Math.sqrt(sumSq);
            for (int i = 0; i < dimensions; i++) {
                vector[i] = (float) (vector[i] / norm);
            }
        }
        return vector;
    }

    private String hashText(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(text.hashCode());
        }
    }
}
