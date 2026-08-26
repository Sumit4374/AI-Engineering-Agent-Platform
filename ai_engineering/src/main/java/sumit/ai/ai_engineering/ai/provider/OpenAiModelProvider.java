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
            @Value("${spring.ai.openai.chat.model:${ai.openai.model:${CHAT_MODEL:gemini-3.6-flash}}}") String modelName,
            @Value("${spring.ai.openai.api-key:${ai.openai.api-key:${OPEN_API_KEY:${OPENAI_API_KEY:}}}}") String apiKey) {
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
