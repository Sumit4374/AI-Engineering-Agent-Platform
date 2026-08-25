package sumit.ai.ai_engineering.rag.ingestion;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sumit.ai.ai_engineering.common.exception.ForbiddenAccessException;
import sumit.ai.ai_engineering.common.exception.ResourceNotFoundException;
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

@Service
@Transactional
public class DocumentIngestionServiceImpl implements DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionServiceImpl.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private final DocumentRepository documentRepository;
    private final List<DocumentExtractor> extractors;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    public DocumentIngestionServiceImpl(
            DocumentRepository documentRepository,
            List<DocumentExtractor> extractors,
            ChunkingService chunkingService,
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService) {
        this.documentRepository = documentRepository;
        this.extractors = extractors;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
    }

    @Override
    public Document ingestDocument(Long userId, String fileName, String contentType, byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("File content must not be empty");
        }
        if (data.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of 10 MB");
        }

        DocumentType docType = DocumentType.fromFilename(fileName);
        Document document = Document.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .fileName(fileName)
                .contentType(contentType != null ? contentType : "application/octet-stream")
                .documentType(docType)
                .fileSize((long) data.length)
                .status(DocumentStatus.PROCESSING)
                .build();
        document = documentRepository.save(document);

        try {
            // Extract text
            DocumentExtractor extractor = findExtractor(docType);
            String text = extractor.extractText(new ByteArrayInputStream(data), fileName);

            if (text == null || text.isBlank()) {
                document.setStatus(DocumentStatus.READY);
                document.setTotalChunks(0);
                return documentRepository.save(document);
            }

            // Chunk text
            List<Chunk> chunks = chunkingService.chunk(document.getId(), userId, text, fileName, docType, 400, 50);

            // Generate embeddings in batch
            List<String> chunkTexts = chunks.stream().map(Chunk::content).toList();
            List<float[]> embeddings = embeddingService.embedBatch(chunkTexts);

            // Build DocumentChunk entities
            List<DocumentChunk> docChunks = new ArrayList<>(chunks.size());
            for (int i = 0; i < chunks.size(); i++) {
                Chunk c = chunks.get(i);
                float[] emb = i < embeddings.size() ? embeddings.get(i) : new float[0];
                DocumentChunk dc = DocumentChunk.builder()
                        .id(UUID.randomUUID())
                        .documentId(document.getId())
                        .userId(userId)
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

            // Save to vector store
            vectorStoreService.storeChunks(docChunks);

            document.setStatus(DocumentStatus.READY);
            document.setTotalChunks(docChunks.size());
            Document saved = documentRepository.save(document);

            log.info("Successfully ingested document [id={}, fileName={}, chunks={}] for userId={}",
                    saved.getId(), fileName, saved.getTotalChunks(), userId);

            return saved;
        } catch (Exception e) {
            log.error("Failed to ingest document [id={}, fileName={}]: {}", document.getId(), fileName, e.getMessage(), e);
            document.setStatus(DocumentStatus.FAILED);
            documentRepository.save(document);
            throw new RuntimeException("Document ingestion failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Document ingestRawText(Long userId, String title, String content) {
        String filename = (title != null && !title.isBlank()) ? title : "raw_text_" + UUID.randomUUID() + ".txt";
        if (!filename.contains(".")) {
            filename += ".txt";
        }
        byte[] bytes = (content != null ? content : "").getBytes(StandardCharsets.UTF_8);
        return ingestDocument(userId, filename, "text/plain", bytes);
    }

    @Override
    public void deleteDocument(Long userId, UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        if (!document.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You do not have permission to delete document: " + documentId);
        }

        vectorStoreService.deleteByDocumentId(documentId);
        documentRepository.delete(document);
        log.info("Deleted document [id={}, userId={}]", documentId, userId);
    }

    private DocumentExtractor findExtractor(DocumentType type) {
        return extractors.stream()
                .filter(e -> e.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported document type: " + type));
    }
}
