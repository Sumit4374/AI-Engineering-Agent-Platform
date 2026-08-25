package sumit.ai.ai_engineering.events.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.events.model.AgentExecutionCompletedEvent;

@Component
public class AuditLogConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditLogConsumer.class);

    @EventListener
    @KafkaListener(topics = "agent.completed", autoStartup = "${kafka.enabled:false}")
    public void onAgentExecutionCompleted(AgentExecutionCompletedEvent event) {
        log.info("Audit: AgentExecutionCompleted [executionId={}, userId={}, goal='{}', status={}, iterations={}]",
                event.executionId(), event.userId(), event.goal(), event.status(), event.iterations());
    }
}
