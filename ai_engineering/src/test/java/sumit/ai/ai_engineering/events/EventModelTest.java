package sumit.ai.ai_engineering.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import sumit.ai.ai_engineering.events.model.AgentExecutionCompletedEvent;
import sumit.ai.ai_engineering.events.model.ConversationCompletedEvent;
import sumit.ai.ai_engineering.events.model.DocumentIngestedEvent;
import sumit.ai.ai_engineering.events.model.DocumentUploadedEvent;

class EventModelTest {

    @Test
    void documentUploadedEvent_createsWithCorrectEventType() {
        UUID docId = UUID.randomUUID();
        DocumentUploadedEvent event = DocumentUploadedEvent.of(1L, docId, "test.pdf", "application/pdf", new byte[]{1, 2, 3});

        assertThat(event.eventType()).isEqualTo("document.uploaded");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.documentId()).isEqualTo(docId);
        assertThat(event.timestamp()).isNotNull();
    }

    @Test
    void documentIngestedEvent_createsWithCorrectEventType() {
        UUID docId = UUID.randomUUID();
        DocumentIngestedEvent event = DocumentIngestedEvent.of(1L, docId, "test.pdf", 4, "READY");

        assertThat(event.eventType()).isEqualTo("document.ingested");
        assertThat(event.totalChunks()).isEqualTo(4);
    }

    @Test
    void conversationCompletedEvent_createsWithCorrectEventType() {
        UUID convId = UUID.randomUUID();
        ConversationCompletedEvent event = ConversationCompletedEvent.of(2L, convId, 12);

        assertThat(event.eventType()).isEqualTo("conversation.completed");
        assertThat(event.messageCount()).isEqualTo(12);
    }

    @Test
    void agentExecutionCompletedEvent_createsWithCorrectEventType() {
        UUID execId = UUID.randomUUID();
        AgentExecutionCompletedEvent event = AgentExecutionCompletedEvent.of(3L, execId, "Analyze code", "COMPLETED", 5);

        assertThat(event.eventType()).isEqualTo("agent.completed");
        assertThat(event.iterations()).isEqualTo(5);
    }
}
