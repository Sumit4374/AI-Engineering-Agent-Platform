package sumit.ai.ai_engineering.rag.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import sumit.ai.ai_engineering.rag.model.Document;
import sumit.ai.ai_engineering.rag.model.DocumentStatus;
import sumit.ai.ai_engineering.rag.model.DocumentType;

public record DocumentDTO(
    UUID id,
    Long userId,
    String fileName,
    String contentType,
    DocumentType documentType,
    Long fileSize,
    DocumentStatus status,
    Integer totalChunks,
    LocalDateTime createdAt
) {
    public static DocumentDTO from(Document d) {
        return new DocumentDTO(
            d.getId(),
            d.getUserId(),
            d.getFileName(),
            d.getContentType(),
            d.getDocumentType(),
            d.getFileSize(),
            d.getStatus(),
            d.getTotalChunks(),
            d.getCreatedAt()
        );
    }
}
