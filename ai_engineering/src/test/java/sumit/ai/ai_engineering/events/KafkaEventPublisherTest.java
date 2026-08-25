package sumit.ai.ai_engineering.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import sumit.ai.ai_engineering.events.model.ConversationCompletedEvent;
import sumit.ai.ai_engineering.events.publisher.KafkaEventPublisher;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock private ApplicationEventPublisher localPublisher;
    @Mock private ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;

    private KafkaEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new KafkaEventPublisher(kafkaTemplateProvider, localPublisher);
    }

    @Test
    void publish_nullEvent_doesNothing() {
        publisher.publish(null);
        verify(localPublisher, org.mockito.Mockito.never()).publishEvent(any());
    }

    @Test
    void publish_withKafkaAvailable_sendsToKafkaAndLocalPublisher() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplateProvider.getIfAvailable()).thenReturn(kafkaTemplate);

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq("conversation.completed"), any(), any())).thenReturn(future);

        ConversationCompletedEvent event = ConversationCompletedEvent.of(1L, UUID.randomUUID(), 10);

        publisher.publish(event);

        verify(localPublisher).publishEvent(event);
        verify(kafkaTemplate).send(eq("conversation.completed"), eq(event.eventId().toString()), eq(event));
    }

    @Test
    void publish_whenKafkaUnavailable_stillPublishesLocally() {
        when(kafkaTemplateProvider.getIfAvailable()).thenReturn(null);

        ConversationCompletedEvent event = ConversationCompletedEvent.of(1L, UUID.randomUUID(), 5);

        publisher.publish(event);

        verify(localPublisher).publishEvent(event);
    }
}
