package sumit.ai.ai_engineering.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

import sumit.ai.ai_engineering.events.consumer.DocumentIngestionConsumer;
import sumit.ai.ai_engineering.events.model.DocumentIngestedEvent;
import sumit.ai.ai_engineering.events.model.DocumentUploadedEvent;
import sumit.ai.ai_engineering.events.publisher.EventPublisher;
import sumit.ai.ai_engineering.rag.chunking.Chunk;
import sumit.ai.ai_engineering.rag.chunking.ChunkingService;
import sumit.ai.ai_engineering.rag.embedding.EmbeddingService;
import sumit.ai.ai_engineering.rag.ingestion.extractor.DocumentExtractor;
import sumit.ai.ai_engineering.rag.ingestion.extractor.TextDocumentExtractor;
import sumit.ai.ai_engineering.rag.model.ChunkMetadata;
import sumit.ai.ai_engineering.rag.model.Document;
import sumit.ai.ai_engineering.rag.model.DocumentRepository;
import sumit.ai.ai_engineering.rag.model.DocumentStatus;
import sumit.ai.ai_engineering.rag.model.DocumentType;
import sumit.ai.ai_engineering.rag.vector.VectorStoreService;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionConsumerTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private ChunkingService chunkingService;
    @Mock private EmbeddingService embeddingService;
    @Mock private VectorStoreService vectorStoreService;
    @Mock private EventPublisher eventPublisher;

    private DocumentIngestionConsumer consumer;

    private final Long userId = 1L;
    private final UUID docId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        List<DocumentExtractor> extractors = List.of(new TextDocumentExtractor());
        consumer = new DocumentIngestionConsumer(
                documentRepository,
                extractors,
                chunkingService,
                embeddingService,
                vectorStoreService,
                eventPublisher
        );
    }

    @Test
    void onDocumentUploaded_processesAndPublishesIngestedEvent() {
        byte[] data = "Sample text content for async ingestion".getBytes(StandardCharsets.UTF_8);
        DocumentUploadedEvent event = DocumentUploadedEvent.of(userId, docId, "doc.txt", "text/plain", data);

        Document doc = Document.builder()
                .id(docId)
                .userId(userId)
                .fileName("doc.txt")
                .contentType("text/plain")
                .documentType(DocumentType.TEXT)
                .status(DocumentStatus.PENDING)
                .totalChunks(0)
                .build();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        Chunk chunk = new Chunk(0, "Sample text content", 10, new ChunkMetadata(docId, UUID.randomUUID(), "doc.txt", "doc.txt", null, "text", LocalDateTime.now()));
        when(chunkingService.chunk(eq(docId), eq(userId), any(), eq("doc.txt"), eq(DocumentType.TEXT), anyInt(), anyInt()))
                .thenReturn(List.of(chunk));
        when(embeddingService.embedBatch(any())).thenReturn(List.of(new float[1536]));

        consumer.onDocumentUploaded(event);

        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.READY);
        assertThat(doc.getTotalChunks()).isEqualTo(1);
        verify(vectorStoreService).storeChunks(any());
        verify(eventPublisher).publish(eq("document.ingested"), any(DocumentIngestedEvent.class));
    }

    @Test
    void onDocumentUploaded_alreadyReady_skipsDuplicateProcessing() {
        DocumentUploadedEvent event = DocumentUploadedEvent.of(userId, docId, "doc.txt", "text/plain", new byte[10]);

        Document readyDoc = Document.builder()
                .id(docId)
                .userId(userId)
                .fileName("doc.txt")
                .status(DocumentStatus.READY)
                .totalChunks(5)
                .build();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(readyDoc));

        consumer.onDocumentUploaded(event);

        verify(chunkingService, never()).chunk(any(), any(), any(), any(), any(), anyInt(), anyInt());
        verify(vectorStoreService, never()).storeChunks(any());
    }
}
