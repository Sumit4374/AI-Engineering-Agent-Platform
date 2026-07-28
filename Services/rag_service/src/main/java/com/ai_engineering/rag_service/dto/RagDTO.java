package com.ai_engineering.rag_service.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

/**
 * Request/response records for the RAG API. Nested records mirror the DTO
 * convention used across the platform (see {@code ai_service}'s {@code ChatDTO}).
 */
public class RagDTO {

    /** Result of ingesting one document. */
    public record IngestResponse(
            String docId,
            String filename,
            int chunkCount
    ) {}

    /**
     * Retrieval request. {@code docIds} is optional — when present, the search
     * is further restricted to those documents; when null/empty, all documents
     * in the conversation are searched.
     */
    public record RetrieveRequest(
            @NotBlank(message = "query is required")
            String query,
            @NotBlank(message = "conversationId is required")
            String conversationId,
            List<String> docIds,
            Integer topK
    ) {}

    /** A single retrieved chunk with its similarity score. */
    public record Chunk(
            String text,
            String docId,
            String filename,
            Double score
    ) {}

    public record RetrieveResponse(
            List<Chunk> chunks
    ) {}

    /** Result of deleting a document's chunks. */
    public record DeleteResponse(
            String docId,
            String status
    ) {}
}
