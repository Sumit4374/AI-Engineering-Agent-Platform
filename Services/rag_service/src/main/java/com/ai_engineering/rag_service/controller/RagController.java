package com.ai_engineering.rag_service.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai_engineering.rag_service.dto.RagDTO.DeleteResponse;
import com.ai_engineering.rag_service.dto.RagDTO.IngestResponse;
import com.ai_engineering.rag_service.dto.RagDTO.RetrieveRequest;
import com.ai_engineering.rag_service.dto.RagDTO.RetrieveResponse;
import com.ai_engineering.rag_service.service.DocumentIngestionService;
import com.ai_engineering.rag_service.service.RetrievalService;

import jakarta.validation.Valid;

/**
 * RAG API: ingest documents and retrieve relevant chunks. Every operation is
 * scoped by {@code conversationId} so documents stay isolated per conversation.
 *
 * <p>This service is internal (reached via the agent, not the public gateway),
 * so it does not authenticate directly; the caller is trusted to pass the
 * already-scoped conversation id.
 */
@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    private final DocumentIngestionService ingestionService;
    private final RetrievalService retrievalService;

    public RagController(DocumentIngestionService ingestionService,
                         RetrievalService retrievalService) {
        this.ingestionService = ingestionService;
        this.retrievalService = retrievalService;
    }

    /** Upload + ingest a document for a conversation. */
    @PostMapping("/documents")
    public ResponseEntity<IngestResponse> ingest(
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversationId") String conversationId) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required and must not be empty");
        }
        IngestResponse response = ingestionService.ingest(
                file.getBytes(), file.getOriginalFilename(), conversationId);
        return ResponseEntity.ok(response);
    }

    /** Retrieve the most relevant chunks for a query within a conversation. */
    @PostMapping("/retrieve")
    public ResponseEntity<RetrieveResponse> retrieve(@Valid @RequestBody RetrieveRequest req) {
        return ResponseEntity.ok(new RetrieveResponse(
                retrievalService.retrieve(req.query(), req.conversationId(), req.docIds(), req.topK())));
    }

    /** Delete a document's chunks from a conversation. */
    @DeleteMapping("/documents/{docId}")
    public ResponseEntity<DeleteResponse> delete(
            @PathVariable String docId,
            @RequestParam("conversationId") String conversationId) {

        ingestionService.deleteDocument(docId, conversationId);
        return ResponseEntity.ok(new DeleteResponse(docId, "deleted"));
    }
}
