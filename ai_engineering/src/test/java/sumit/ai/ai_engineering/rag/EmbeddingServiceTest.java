package sumit.ai.ai_engineering.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.ai.openai.OpenAiEmbeddingModel;

import sumit.ai.ai_engineering.rag.embedding.OpenAiEmbeddingService;

class EmbeddingServiceTest {

    private OpenAiEmbeddingService service;

    @BeforeEach
    void setUp() {
        @SuppressWarnings("unchecked")
        ObjectProvider<OpenAiEmbeddingModel> provider = mock(ObjectProvider.class);
        service = new OpenAiEmbeddingService(provider);
    }

    @Test
    void embed_emptyOrNullText_returnsZeroVector() {
        float[] v1 = service.embed(null);
        float[] v2 = service.embed("   ");

        assertThat(v1).hasSize(1536);
        assertThat(v2).hasSize(1536);
        assertThat(v1[0]).isEqualTo(0.0f);
    }

    @Test
    void embed_generatesDeterministicNormalizedVector() {
        float[] v1 = service.embed("Machine learning and artificial intelligence");
        float[] v2 = service.embed("Machine learning and artificial intelligence");

        assertThat(v1).hasSize(1536);
        assertThat(v1).isEqualTo(v2);

        // Check L2 normalization (length ≈ 1.0)
        double sumSq = 0.0;
        for (float f : v1) {
            sumSq += f * f;
        }
        assertThat(Math.sqrt(sumSq)).isBetween(0.99, 1.01);
    }

    @Test
    void embed_differentTexts_produceDifferentVectors() {
        float[] v1 = service.embed("Distributed systems architecture");
        float[] v2 = service.embed("Baking chocolate chip cookies");

        assertThat(v1).isNotEqualTo(v2);
    }

    @Test
    void embedBatch_returnsVectorForEachInput() {
        List<float[]> results = service.embedBatch(List.of("Doc 1", "Doc 2", "Doc 3"));

        assertThat(results).hasSize(3);
        assertThat(results.get(0)).hasSize(1536);
        assertThat(results.get(1)).hasSize(1536);
    }

    @Test
    void getDimensions_returns1536() {
        assertThat(service.getDimensions()).isEqualTo(1536);
    }
}
