package sumit.ai.ai_engineering.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sumit.ai.ai_engineering.rag.chunking.Chunk;
import sumit.ai.ai_engineering.rag.chunking.ConfigurableChunker;
import sumit.ai.ai_engineering.rag.model.DocumentType;

class ChunkingServiceTest {

    private ConfigurableChunker chunker;
    private final UUID docId = UUID.randomUUID();
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        chunker = new ConfigurableChunker();
    }

    @Test
    void chunk_emptyOrNullText_returnsEmptyList() {
        assertThat(chunker.chunk(docId, userId, null, "test.txt", DocumentType.TEXT, 100, 10)).isEmpty();
        assertThat(chunker.chunk(docId, userId, "   ", "test.txt", DocumentType.TEXT, 100, 10)).isEmpty();
    }

    @Test
    void chunk_smallText_returnsSingleChunk() {
        String text = "This is a short document that easily fits in one chunk.";
        List<Chunk> chunks = chunker.chunk(docId, userId, text, "doc.txt", DocumentType.TEXT, 100, 10);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).content()).isEqualTo(text);
        assertThat(chunks.get(0).metadata().documentId()).isEqualTo(docId);
        assertThat(chunks.get(0).metadata().language()).isEqualTo("text");
    }

    @Test
    void chunk_largeText_splitsIntoMultipleChunksWithOverlap() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 50; i++) {
            sb.append("Sentence ").append(i).append(". This provides paragraph content for chunking tests. ");
            if (i % 5 == 0) sb.append("\n\n");
        }
        String text = sb.toString();

        List<Chunk> chunks = chunker.chunk(docId, userId, text, "guide.md", DocumentType.MARKDOWN, 50, 10);

        assertThat(chunks.size()).isGreaterThan(1);
        assertThat(chunks.get(0).metadata().language()).isEqualTo("markdown");
        for (int i = 0; i < chunks.size(); i++) {
            assertThat(chunks.get(i).index()).isEqualTo(i);
            assertThat(chunks.get(i).content()).isNotBlank();
        }
    }

    @Test
    void chunk_sourceCode_infersCodeLanguage() {
        String javaCode = """
                public class Calculator {
                    public int add(int a, int b) {
                        return a + b;
                    }
                }
                """;
        List<Chunk> chunks = chunker.chunk(docId, userId, javaCode, "Calculator.java", DocumentType.CODE, 100, 10);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).metadata().language()).isEqualTo("java");
    }
}
