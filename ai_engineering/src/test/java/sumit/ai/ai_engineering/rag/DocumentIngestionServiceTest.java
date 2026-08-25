package sumit.ai.ai_engineering.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sumit.ai.ai_engineering.common.exception.ForbiddenAccessException;
import sumit.ai.ai_engineering.common.exception.ResourceNotFoundException;
import sumit.ai.ai_engineering.rag.chunking.Chunk;
import sumit.ai.ai_engineering.rag.chunking.ChunkingService;
import sumit.ai.ai_engineering.rag.embedding.EmbeddingService;
import sumit.ai.ai_engineering.rag.ingestion.DocumentIngestionServiceImpl;
import sumit.ai.ai_engineering.rag.ingestion.extractor.CodeDocumentExtractor;
import sumit.ai.ai_engineering.rag.ingestion.extractor.DocumentExtractor;
import sumit.ai.ai_engineering.rag.ingestion.extractor.TextDocumentExtractor;
import sumit.ai.ai_engineering.rag.model.ChunkMetadata;
import sumit.ai.ai_engineering.rag.model.Document;
import sumit.ai.ai_engineering.rag.model.DocumentRepository;
import sumit.ai.ai_engineering.rag.model.DocumentStatus;
import sumit.ai.ai_engineering.rag.model.DocumentType;
import sumit.ai.ai_engineering.rag.vector.VectorStoreService;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionServiceTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private ChunkingService chunkingService;
    @Mock private EmbeddingService embeddingService;
    @Mock private VectorStoreService vectorStoreService;

    private DocumentIngestionServiceImpl ingestionService;

    private final Long userId = 1L;
    private final UUID docId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        List<DocumentExtractor> extractors = List.of(
                new TextDocumentExtractor(),
                new CodeDocumentExtractor()
        );
        ingestionService = new DocumentIngestionServiceImpl(
                documentRepository,
                extractors,
                chunkingService,
                embeddingService,
                vectorStoreService
        );
    }

    @Test
    void ingestDocument_textDocument_extractsChunksAndStoresVectors() {
        String content = "Spring Boot makes building robust Java microservices and monoliths easy.";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        Document doc = Document.builder()
                .id(docId)
                .userId(userId)
                .fileName("intro.md")
                .documentType(DocumentType.MARKDOWN)
                .contentType("text/markdown")
                .fileSize((long) bytes.length)
                .status(DocumentStatus.PROCESSING)
                .build();

        when(documentRepository.save(any(Document.class))).thenReturn(doc);

        Chunk chunk = new Chunk(0, content, 15, new ChunkMetadata(docId, UUID.randomUUID(), "intro.md", "intro.md", null, "markdown", LocalDateTime.now()));
        when(chunkingService.chunk(eq(docId), eq(userId), any(), eq("intro.md"), eq(DocumentType.MARKDOWN), anyInt(), anyInt()))
                .thenReturn(List.of(chunk));
        when(embeddingService.embedBatch(any())).thenReturn(List.of(new float[1536]));

        Document result = ingestionService.ingestDocument(userId, "intro.md", "text/markdown", bytes);

        assertThat(result.getStatus()).isEqualTo(DocumentStatus.READY);
        assertThat(result.getTotalChunks()).isEqualTo(1);
        verify(vectorStoreService).storeChunks(any());
        verify(documentRepository).save(doc);
    }

    @Test
    void ingestDocument_emptyBytes_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> ingestionService.ingestDocument(userId, "empty.txt", "text/plain", new byte[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteDocument_ownedByUser_deletesDocumentAndVectors() {
        Document doc = Document.builder().id(docId).userId(userId).build();
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        ingestionService.deleteDocument(userId, docId);

        verify(vectorStoreService).deleteByDocumentId(docId);
        verify(documentRepository).delete(doc);
    }

    @Test
    void deleteDocument_notOwnedByUser_throwsForbiddenAccessException() {
        Document doc = Document.builder().id(docId).userId(999L).build();
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> ingestionService.deleteDocument(userId, docId))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void deleteDocument_notFound_throwsResourceNotFoundException() {
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingestionService.deleteDocument(userId, docId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
