package com.ai_engineering.rag_service.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.ai_engineering.rag_service.dto.RagDTO.IngestResponse;

/**
 * Ingests an uploaded document: parse → chunk → embed → store.
 *
 * <p>Each chunk is stamped with {@code docId} (SHA-256 of the file bytes),
 * {@code conversationId}, {@code filename}, and {@code chunkIndex}. The
 * conversation stamp is what lets {@link RetrievalService} isolate one
 * conversation's documents from another's at query time.
 */
@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    public static final String META_DOC_ID = "docId";
    public static final String META_CONVERSATION_ID = "conversationId";
    public static final String META_FILENAME = "filename";
    public static final String META_CHUNK_INDEX = "chunkIndex";

    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter = TokenTextSplitter.builder().build();

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public IngestResponse ingest(byte[] content, String filename, String conversationId) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId is required");
        }

        String docId = sha256(content);
        String safeName = (filename == null || filename.isBlank()) ? docId : filename;

        // Parse (Tika handles PDF/DOCX/HTML/plain text). Name the resource so
        // Tika's source metadata reflects the real filename.
        Resource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return safeName;
            }
        };
        List<Document> parsed = new TikaDocumentReader(resource).get();

        // Split into token-bounded chunks.
        List<Document> chunks = splitter.split(parsed);

        // Stamp isolation + provenance metadata on every chunk.
        int idx = 0;
        for (Document chunk : chunks) {
            chunk.getMetadata().put(META_DOC_ID, docId);
            chunk.getMetadata().put(META_CONVERSATION_ID, conversationId);
            chunk.getMetadata().put(META_FILENAME, safeName);
            chunk.getMetadata().put(META_CHUNK_INDEX, idx++);
        }

        // add() embeds + persists in one call.
        vectorStore.add(chunks);

        log.info("Ingested docId={} ({}), {} chunks, conversationId={}",
                docId, safeName, chunks.size(), conversationId);
        return new IngestResponse(docId, safeName, chunks.size());
    }

    /**
     * Removes every stored chunk belonging to {@code docId}. Scoped to the
     * given conversation so one conversation cannot delete another's copy of a
     * shared document.
     */
    public void deleteDocument(String docId, String conversationId) {
        if (docId == null || docId.isBlank()) {
            throw new IllegalArgumentException("docId is required");
        }
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId is required");
        }
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression expression = b.and(
                b.eq(META_DOC_ID, docId),
                b.eq(META_CONVERSATION_ID, conversationId)).build();
        vectorStore.delete(expression);
        log.info("Deleted docId={} for conversationId={}", docId, conversationId);
    }

    private String sha256(byte[] content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed present on every JVM.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
