package sumit.ai.ai_engineering.events.model;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BaseEvent {
    UUID eventId();
    LocalDateTime timestamp();
    String eventType();
}
