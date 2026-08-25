package sumit.ai.ai_engineering.ai.provider;

import org.springframework.ai.chat.client.ChatClient;

public interface ModelProvider {

    ModelProviderType getProviderType();

    String getProviderName();

    String getDefaultModel();

    boolean isAvailable();

    ChatClient getChatClient();
}
