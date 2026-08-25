package sumit.ai.ai_engineering.events.publisher;

import sumit.ai.ai_engineering.events.model.BaseEvent;

public interface EventPublisher {

    void publish(BaseEvent event);

    void publish(String topic, BaseEvent event);
}
