package sumit.ai.ai_engineering.rag.api.dto;

import java.util.UUID;

import sumit.ai.ai_engineering.rag.model.DocumentStatus;

public record DocumentUploadResponse(
    UUID id,
    String fileName,
    DocumentStatus status,
    int totalChunks,
    String message
) {}
