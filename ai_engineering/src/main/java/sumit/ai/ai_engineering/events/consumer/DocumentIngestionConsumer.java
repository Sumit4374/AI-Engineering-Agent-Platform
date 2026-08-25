package sumit.ai.ai_engineering.events.consumer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.events.model.DocumentIngestedEvent;
import sumit.ai.ai_engineering.events.model.DocumentUploadedEvent;
import sumit.ai.ai_engineering.events.publisher.EventPublisher;
import sumit.ai.ai_engineering.rag.chunking.Chunk;
import sumit.ai.ai_engineering.rag.chunking.ChunkingService;
import sumit.ai.ai_engineering.rag.embedding.EmbeddingService;
import sumit.ai.ai_engineering.rag.ingestion.extractor.DocumentExtractor;
import sumit.ai.ai_engineering.rag.model.Document;
import sumit.ai.ai_engineering.rag.model.DocumentChunk;
import sumit.ai.ai_engineering.rag.model.DocumentRepository;
import sumit.ai.ai_engineering.rag.model.DocumentStatus;
import sumit.ai.ai_engineering.rag.model.DocumentType;
import sumit.ai.ai_engineering.rag.vector.PgVectorStoreService;
import sumit.ai.ai_engineering.rag.vector.VectorStoreService;

@Component
public class DocumentIngestionConsumer {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionConsumer.class);

    private final DocumentRepository documentRepository;
    private final List<DocumentExtractor> extractors;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final EventPublisher eventPublisher;

    public DocumentIngestionConsumer(
            DocumentRepository documentRepository,
            List<DocumentExtractor> extractors,
            ChunkingService chunkingService,
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService,
            EventPublisher eventPublisher) {
        this.documentRepository = documentRepository;
        this.extractors = extractors;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @KafkaListener(topics = "document.uploaded", autoStartup = "${kafka.enabled:false}")
    public void onDocumentUploaded(DocumentUploadedEvent event) {
        log.info("Received DocumentUploadedEvent [documentId={}, fileName={}, userId={}]",
                event.documentId(), event.fileName(), event.userId());

        Document document = documentRepository.findById(event.documentId()).orElse(null);
        if (document == null) {
            log.warn("Document not found for event [documentId={}]", event.documentId());
            return;
        }

        // Idempotency check: if already READY or chunk count > 0, skip
        if (document.getStatus() == DocumentStatus.READY && document.getTotalChunks() > 0) {
            log.info("Document [id={}] is already processed. Skipping duplicate event.", event.documentId());
            return;
        }

        try {
            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);

            DocumentType docType = document.getDocumentType();
            DocumentExtractor extractor = extractors.stream()
                    .filter(e -> e.supports(docType))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No extractor for " + docType));

            String text = extractor.extractText(new ByteArrayInputStream(event.data()), event.fileName());
            if (text == null || text.isBlank()) {
                document.setStatus(DocumentStatus.READY);
                document.setTotalChunks(0);
                documentRepository.save(document);
                publishCompletion(event, 0, "READY");
                return;
            }

            List<Chunk> chunks = chunkingService.chunk(document.getId(), event.userId(), text, event.fileName(),
                    docType, 400, 50);
            List<String> chunkTexts = chunks.stream().map(Chunk::content).toList();
            List<float[]> embeddings = embeddingService.embedBatch(chunkTexts);

            List<DocumentChunk> docChunks = new ArrayList<>(chunks.size());
            for (int i = 0; i < chunks.size(); i++) {
                Chunk c = chunks.get(i);
                float[] emb = i < embeddings.size() ? embeddings.get(i) : new float[0];
                DocumentChunk dc = DocumentChunk.builder()
                        .id(UUID.randomUUID())
                        .documentId(document.getId())
                        .userId(event.userId())
                        .chunkIndex(c.index())
                        .content(c.content())
                        .embedding(PgVectorStoreService.serializeVector(emb))
                        .source(c.metadata().source())
                        .fileName(c.metadata().fileName())
                        .page(c.metadata().page())
                        .language(c.metadata().language())
                        .build();
                docChunks.add(dc);
            }

            vectorStoreService.storeChunks(docChunks);

            document.setStatus(DocumentStatus.READY);
            document.setTotalChunks(docChunks.size());
            documentRepository.save(document);

            log.info("Async ingestion completed for document [id={}, totalChunks={}]",
                    document.getId(), docChunks.size());

            publishCompletion(event, docChunks.size(), "READY");

        } catch (Exception e) {
            log.error("Async document ingestion failed for [id={}]: {}", event.documentId(), e.getMessage(), e);
            document.setStatus(DocumentStatus.FAILED);
            documentRepository.save(document);
            publishCompletion(event, 0, "FAILED");
        }
    }

    private void publishCompletion(DocumentUploadedEvent event, int totalChunks, String status) {
        DocumentIngestedEvent ingestedEvent = DocumentIngestedEvent.of(
                event.userId(),
                event.documentId(),
                event.fileName(),
                totalChunks,
                status);
        eventPublisher.publish("document.ingested", ingestedEvent);
    }
}
