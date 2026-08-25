package sumit.ai.ai_engineering.rag.ingestion;

import java.util.UUID;

import sumit.ai.ai_engineering.rag.model.Document;

public interface DocumentIngestionService {

    Document ingestDocument(Long userId, String fileName, String contentType, byte[] data);

    Document ingestRawText(Long userId, String title, String content);

    void deleteDocument(Long userId, UUID documentId);
}
