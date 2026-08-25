package sumit.ai.ai_engineering.events.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentIngestedEvent(
    UUID eventId,
    Long userId,
    UUID documentId,
    String fileName,
    int totalChunks,
    String status,
    LocalDateTime timestamp
) implements BaseEvent {

    public static DocumentIngestedEvent of(Long userId, UUID documentId, String fileName, int totalChunks, String status) {
        return new DocumentIngestedEvent(
            UUID.randomUUID(),
            userId,
            documentId,
            fileName,
            totalChunks,
            status,
            LocalDateTime.now()
        );
    }

    @Override
    public String eventType() {
        return "document.ingested";
    }
}
