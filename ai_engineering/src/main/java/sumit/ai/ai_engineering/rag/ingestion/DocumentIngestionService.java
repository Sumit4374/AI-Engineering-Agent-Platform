package sumit.ai.ai_engineering.rag.ingestion;

import java.util.UUID;

import sumit.ai.ai_engineering.rag.model.Document;

public interface DocumentIngestionService {

    Document ingestDocument(Long userId, String fileName, String contentType, byte[] data);

    Document ingestRawText(Long userId, String title, String content);

    java.util.List<Document> listDocuments(Long userId);

    Document getDocument(Long userId, UUID documentId);

    void deleteDocument(Long userId, UUID documentId);
}
