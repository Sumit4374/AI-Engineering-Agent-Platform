package sumit.ai.ai_engineering.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sumit.ai.ai_engineering.rag.model.DocumentChunk;
import sumit.ai.ai_engineering.rag.model.DocumentChunkRepository;
import sumit.ai.ai_engineering.rag.model.RetrievedChunk;
import sumit.ai.ai_engineering.rag.vector.PgVectorStoreService;

@ExtendWith(MockitoExtension.class)
class PgVectorStoreServiceTest {

    @Mock
    private DocumentChunkRepository chunkRepository;

    private PgVectorStoreService vectorStore;

    private final Long userId = 1L;
    private final UUID docId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        vectorStore = new PgVectorStoreService(chunkRepository);
    }

    @Test
    void vectorSerializationAndParsing_isConsistent() {
        float[] original = new float[]{0.123f, -0.456f, 0.789f};
        String serialized = PgVectorStoreService.serializeVector(original);
        float[] parsed = PgVectorStoreService.parseVector(serialized);

        assertThat(parsed).containsExactly(original);
    }

    @Test
    void cosineSimilarity_identicalVectors_returnsOne() {
        float[] v1 = new float[]{1.0f, 0.0f, 0.0f};
        float[] v2 = new float[]{1.0f, 0.0f, 0.0f};

        double similarity = PgVectorStoreService.cosineSimilarity(v1, v2);

        assertThat(similarity).isBetween(0.999, 1.001);
    }

    @Test
    void cosineSimilarity_orthogonalVectors_returnsZero() {
        float[] v1 = new float[]{1.0f, 0.0f};
        float[] v2 = new float[]{0.0f, 1.0f};

        double similarity = PgVectorStoreService.cosineSimilarity(v1, v2);

        assertThat(similarity).isEqualTo(0.0);
    }

    @Test
    void searchSimilar_filtersByUserIdAndRanksByScore() {
        float[] query = new float[]{1.0f, 0.0f, 0.0f};

        // Chunk 1: High similarity (score 1.0)
        DocumentChunk c1 = DocumentChunk.builder()
                .id(UUID.randomUUID())
                .documentId(docId)
                .userId(userId)
                .content("Matching content")
                .embedding(PgVectorStoreService.serializeVector(new float[]{1.0f, 0.0f, 0.0f}))
                .fileName("doc1.txt")
                .build();

        // Chunk 2: Medium similarity (score ~0.7)
        DocumentChunk c2 = DocumentChunk.builder()
                .id(UUID.randomUUID())
                .documentId(docId)
                .userId(userId)
                .content("Partial match")
                .embedding(PgVectorStoreService.serializeVector(new float[]{0.707f, 0.707f, 0.0f}))
                .fileName("doc2.txt")
                .build();

        when(chunkRepository.findByUserId(userId)).thenReturn(List.of(c1, c2));

        List<RetrievedChunk> results = vectorStore.searchSimilar(userId, query, 5, 0.5);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).content()).isEqualTo("Matching content");
        assertThat(results.get(0).score()).isGreaterThan(results.get(1).score());
    }

    @Test
    void storeChunks_savesAllChunks() {
        DocumentChunk chunk = DocumentChunk.builder().id(UUID.randomUUID()).documentId(docId).userId(userId).build();

        vectorStore.storeChunks(List.of(chunk));

        verify(chunkRepository).saveAll(any());
    }

    @Test
    void deleteByDocumentId_delegatesToRepository() {
        vectorStore.deleteByDocumentId(docId);

        verify(chunkRepository).deleteByDocumentId(docId);
    }
}
