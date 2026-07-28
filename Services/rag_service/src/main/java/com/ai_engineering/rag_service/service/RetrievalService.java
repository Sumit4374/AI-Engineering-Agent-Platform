package com.ai_engineering.rag_service.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import com.ai_engineering.rag_service.dto.RagDTO.Chunk;

/**
 * Semantic retrieval over stored chunks, always scoped to a single
 * conversation. The {@code conversationId} metadata filter is mandatory — it
 * is what guarantees a conversation can only ever retrieve its own documents.
 * An optional {@code docIds} list narrows the search further.
 */
@Service
public class RetrievalService {

    private static final Logger log = LoggerFactory.getLogger(RetrievalService.class);

    private static final int DEFAULT_TOP_K = 4;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.5;

    private final VectorStore vectorStore;

    public RetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Chunk> retrieve(String query, String conversationId, List<String> docIds, Integer topK) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query is required");
        }
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId is required");
        }

        FilterExpressionBuilder b = new FilterExpressionBuilder();
        // Mandatory conversation isolation.
        FilterExpressionBuilder.Op filter = b.eq(DocumentIngestionService.META_CONVERSATION_ID, conversationId);
        // Optional narrowing to specific documents.
        if (docIds != null && !docIds.isEmpty()) {
            filter = b.and(filter, b.in(DocumentIngestionService.META_DOC_ID, docIds));
        }
        Filter.Expression expression = filter.build();

        int k = (topK != null && topK > 0) ? topK : DEFAULT_TOP_K;

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(k)
                .similarityThreshold(DEFAULT_SIMILARITY_THRESHOLD)
                .filterExpression(expression)
                .build();

        List<Document> results = vectorStore.similaritySearch(request);
        log.debug("Retrieved {} chunks for conversationId={} (topK={})",
                results == null ? 0 : results.size(), conversationId, k);

        if (results == null) {
            return List.of();
        }
        return results.stream().map(this::toChunk).toList();
    }

    private Chunk toChunk(Document doc) {
        Object docId = doc.getMetadata().get(DocumentIngestionService.META_DOC_ID);
        Object filename = doc.getMetadata().get(DocumentIngestionService.META_FILENAME);
        return new Chunk(
                doc.getText(),
                docId == null ? null : docId.toString(),
                filename == null ? null : filename.toString(),
                doc.getScore());
    }
}
