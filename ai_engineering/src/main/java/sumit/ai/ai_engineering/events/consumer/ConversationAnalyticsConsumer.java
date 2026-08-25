package sumit.ai.ai_engineering.events.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.events.model.ConversationCompletedEvent;

@Component
public class ConversationAnalyticsConsumer {

    private static final Logger log = LoggerFactory.getLogger(ConversationAnalyticsConsumer.class);

    @EventListener
    @KafkaListener(topics = "conversation.completed", autoStartup = "${kafka.enabled:false}")
    public void onConversationCompleted(ConversationCompletedEvent event) {
        log.info("Analytics: ConversationCompleted [conversationId={}, userId={}, messageCount={}, timestamp={}]",
                event.conversationId(), event.userId(), event.messageCount(), event.timestamp());
    }
}
