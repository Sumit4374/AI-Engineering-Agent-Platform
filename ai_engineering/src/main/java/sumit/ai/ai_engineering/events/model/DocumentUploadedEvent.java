package sumit.ai.ai_engineering.events.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentUploadedEvent(
    UUID eventId,
    Long userId,
    UUID documentId,
    String fileName,
    String contentType,
    byte[] data,
    LocalDateTime timestamp
) implements BaseEvent {

    public static DocumentUploadedEvent of(Long userId, UUID documentId, String fileName, String contentType, byte[] data) {
        return new DocumentUploadedEvent(
            UUID.randomUUID(),
            userId,
            documentId,
            fileName,
            contentType,
            data,
            LocalDateTime.now()
        );
    }

    @Override
    public String eventType() {
        return "document.uploaded";
    }
}
