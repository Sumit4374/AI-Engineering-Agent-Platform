package sumit.ai.ai_engineering.events.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.events.model.BaseEvent;

@Component
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;
    private final ApplicationEventPublisher localPublisher;

    public KafkaEventPublisher(
            ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider,
            ApplicationEventPublisher localPublisher) {
        this.kafkaTemplateProvider = kafkaTemplateProvider;
        this.localPublisher = localPublisher;
    }

    @Override
    public void publish(BaseEvent event) {
        if (event == null) return;
        publish(event.eventType(), event);
    }

    @Override
    public void publish(String topic, BaseEvent event) {
        if (event == null || topic == null || topic.isBlank()) return;

        // 1. Always publish to Spring local event bus for in-process listeners
        localPublisher.publishEvent(event);

        // 2. Publish to Kafka if available
        KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(topic, event.eventId().toString(), event)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.warn("Kafka send to topic [{}] failed for event [{}]: {}",
                                        topic, event.eventId(), ex.getMessage());
                            } else {
                                log.debug("Kafka event sent to [{}] with offset [{}]",
                                        topic, result.getRecordMetadata().offset());
                            }
                        });
            } catch (Exception e) {
                log.warn("Could not dispatch event to Kafka topic [{}]: {}", topic, e.getMessage());
            }
        }

        log.info("Published domain event [type={}, eventId={}]", event.eventType(), event.eventId());
    }
}
