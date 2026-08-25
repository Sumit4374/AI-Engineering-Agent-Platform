package sumit.ai.ai_engineering.rag.vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sumit.ai.ai_engineering.rag.model.DocumentChunk;
import sumit.ai.ai_engineering.rag.model.DocumentChunkRepository;
import sumit.ai.ai_engineering.rag.model.RetrievedChunk;

@Service
@Transactional
public class PgVectorStoreService implements VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(PgVectorStoreService.class);

    private final DocumentChunkRepository chunkRepository;

    public PgVectorStoreService(DocumentChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    @Override
    public void storeChunks(List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }
        chunkRepository.saveAll(chunks);
        log.debug("Stored {} vector chunks to database", chunks.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetrievedChunk> searchSimilar(Long userId, float[] queryEmbedding, int topK, double minSimilarity) {
        if (userId == null || queryEmbedding == null || queryEmbedding.length == 0) {
            return List.of();
        }

        // Fetch all chunks belonging exclusively to this user
        List<DocumentChunk> userChunks = chunkRepository.findByUserId(userId);
        if (userChunks.isEmpty()) {
            return List.of();
        }

        List<ScoredChunk> scored = new ArrayList<>();

        for (DocumentChunk chunk : userChunks) {
            if (chunk.getEmbedding() == null || chunk.getEmbedding().isBlank()) {
                continue;
            }

            float[] chunkVec = parseVector(chunk.getEmbedding());
            if (chunkVec.length == 0) {
                continue;
            }

            double similarity = cosineSimilarity(queryEmbedding, chunkVec);
            if (similarity >= minSimilarity) {
                scored.add(new ScoredChunk(chunk, similarity));
            }
        }

        // Sort descending by score
        scored.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());

        int limit = Math.min(topK, scored.size());
        List<RetrievedChunk> results = new ArrayList<>(limit);

        for (int i = 0; i < limit; i++) {
            ScoredChunk sc = scored.get(i);
            DocumentChunk c = sc.chunk();
            results.add(new RetrievedChunk(
                    c.getId(),
                    c.getDocumentId(),
                    c.getContent(),
                    sc.score(),
                    c.getFileName(),
                    c.getSource(),
                    c.getPage(),
                    c.getLanguage()
            ));
        }

        log.debug("Found {} relevant chunks for userId={} with topK={}, minSimilarity={}",
                results.size(), userId, topK, minSimilarity);

        return results;
    }

    @Override
    public void deleteByDocumentId(UUID documentId) {
        chunkRepository.deleteByDocumentId(documentId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        List<DocumentChunk> userChunks = chunkRepository.findByUserId(userId);
        chunkRepository.deleteAll(userChunks);
    }

    public static String serializeVector(float[] vector) {
        if (vector == null || vector.length == 0) return "";
        StringBuilder sb = new StringBuilder(vector.length * 8);
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(vector[i]);
        }
        return sb.toString();
    }

    public static float[] parseVector(String vectorStr) {
        if (vectorStr == null || vectorStr.isBlank()) return new float[0];
        String[] tokens = vectorStr.split(",");
        float[] vector = new float[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            try {
                vector[i] = Float.parseFloat(tokens[i].trim());
            } catch (NumberFormatException e) {
                vector[i] = 0.0f;
            }
        }
        return vector;
    }

    public static double cosineSimilarity(float[] vecA, float[] vecB) {
        if (vecA == null || vecB == null || vecA.length == 0 || vecB.length == 0) {
            return 0.0;
        }

        int minLen = Math.min(vecA.length, vecB.length);
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < minLen; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }

        // Account for any remaining dimensions
        for (int i = minLen; i < vecA.length; i++) {
            normA += vecA[i] * vecA[i];
        }
        for (int i = minLen; i < vecB.length; i++) {
            normB += vecB[i] * vecB[i];
        }

        if (normA <= 0.0 || normB <= 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record ScoredChunk(DocumentChunk chunk, double score) {}
}
