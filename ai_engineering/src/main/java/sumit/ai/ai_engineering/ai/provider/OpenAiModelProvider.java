package sumit.ai.ai_engineering.ai.provider;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAiModelProvider implements ModelProvider {

    private final ChatClient chatClient;
    private final String modelName;
    private final String apiKey;

    public OpenAiModelProvider(
            ChatClient chatClient,
            @Value("${ai.openai.model:gpt-4o-mini}") String modelName,
            @Value("${ai.openai.api-key:${OPENAI_API_KEY:}}") String apiKey) {
        this.chatClient = chatClient;
        this.modelName = modelName;
        this.apiKey = apiKey;
    }

    @Override
    public ModelProviderType getProviderType() {
        return ModelProviderType.OPENAI;
    }

    @Override
    public String getProviderName() {
        return "OpenAI Official API";
    }

    @Override
    public String getDefaultModel() {
        return modelName;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public ChatClient getChatClient() {
        return chatClient;
    }
}
