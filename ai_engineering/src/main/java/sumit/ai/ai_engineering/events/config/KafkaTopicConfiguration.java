package sumit.ai.ai_engineering.events.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaTopicConfiguration {

    public static final String DOCUMENT_UPLOADED_TOPIC = "document.uploaded";
    public static final String DOCUMENT_INGESTED_TOPIC = "document.ingested";
    public static final String CONVERSATION_COMPLETED_TOPIC = "conversation.completed";
    public static final String AGENT_COMPLETED_TOPIC = "agent.completed";

    @Bean
    public NewTopic documentUploadedTopic() {
        return TopicBuilder.name(DOCUMENT_UPLOADED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic documentIngestedTopic() {
        return TopicBuilder.name(DOCUMENT_INGESTED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic conversationCompletedTopic() {
        return TopicBuilder.name(CONVERSATION_COMPLETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic agentCompletedTopic() {
        return TopicBuilder.name(AGENT_COMPLETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
