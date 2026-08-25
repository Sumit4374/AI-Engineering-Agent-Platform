package sumit.ai.ai_engineering.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sumit.ai.ai_engineering.ai.engine.AIEngine;
import sumit.ai.ai_engineering.conversation.application.ConversationService;
import sumit.ai.ai_engineering.memory.application.ConversationMemoryService;
import sumit.ai.ai_engineering.rag.embedding.EmbeddingService;
import sumit.ai.ai_engineering.rag.model.RagAnswer;
import sumit.ai.ai_engineering.rag.model.RetrievedChunk;
import sumit.ai.ai_engineering.rag.retrieval.RagRetrievalServiceImpl;
import sumit.ai.ai_engineering.rag.vector.VectorStoreService;

@ExtendWith(MockitoExtension.class)
class RagRetrievalServiceTest {

    @Mock private EmbeddingService embeddingService;
    @Mock private VectorStoreService vectorStoreService;
    @Mock private AIEngine aiEngine;
    @Mock private ConversationService conversationService;
    @Mock private ConversationMemoryService memoryService;

    private RagRetrievalServiceImpl ragService;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        ragService = new RagRetrievalServiceImpl(
                embeddingService,
                vectorStoreService,
                aiEngine,
                conversationService,
                memoryService
        );
    }

    @Test
    void retrieve_embedsQueryAndCallsVectorStore() {
        float[] mockEmbedding = new float[]{0.1f, 0.2f};
        when(embeddingService.embed("how does jwt work?")).thenReturn(mockEmbedding);

        RetrievedChunk chunk = new RetrievedChunk(
                UUID.randomUUID(), UUID.randomUUID(), "JWT is signed with HMAC", 0.88, "auth.md", "auth.md", 1, "markdown"
        );
        when(vectorStoreService.searchSimilar(eq(userId), eq(mockEmbedding), eq(3), eq(0.5)))
                .thenReturn(List.of(chunk));

        List<RetrievedChunk> results = ragService.retrieve(userId, "how does jwt work?", 3, 0.5);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).content()).contains("JWT is signed");
    }

    @Test
    void formatContext_emptyList_returnsFallbackMessage() {
        String formatted = ragService.formatContext(List.of());
        assertThat(formatted).isEqualTo("No relevant document context found.");
    }

    @Test
    void formatContext_withChunks_formatsHeaderAndContent() {
        RetrievedChunk chunk = new RetrievedChunk(
                UUID.randomUUID(), UUID.randomUUID(), "PostgreSQL is a relational database.", 0.92, "db.txt", "db.txt", null, "text"
        );

        String formatted = ragService.formatContext(List.of(chunk));

        assertThat(formatted).contains("Context Fragment 1");
        assertThat(formatted).contains("Source: db.txt");
        assertThat(formatted).contains("PostgreSQL is a relational database.");
    }

    @Test
    void query_generatesAugmentedResponseWithSources() throws IOException {
        float[] mockEmbedding = new float[]{0.1f};
        when(embeddingService.embed(anyString())).thenReturn(mockEmbedding);

        RetrievedChunk chunk = new RetrievedChunk(
                UUID.randomUUID(), UUID.randomUUID(), "Spring AI provides ChatClient", 0.95, "spring.md", "spring.md", null, "markdown"
        );
        when(vectorStoreService.searchSimilar(eq(userId), any(), anyInt(), anyDouble()))
                .thenReturn(List.of(chunk));

        when(aiEngine.generate(anyString(), eq("RAG.prompt"), any(), any(sumit.ai.ai_engineering.ai.tools.model.ToolsCategory[].class)))
                .thenReturn("Spring AI is a framework for AI integration.");

        RagAnswer answer = ragService.query(userId, null, "What is Spring AI?", 5, 0.2);

        assertThat(answer.answer()).isEqualTo("Spring AI is a framework for AI integration.");
        assertThat(answer.sources()).hasSize(1);
        assertThat(answer.totalSourcesFound()).isEqualTo(1);
        verify(aiEngine).generate(anyString(), eq("RAG.prompt"), any(), any(sumit.ai.ai_engineering.ai.tools.model.ToolsCategory[].class));
    }
}
