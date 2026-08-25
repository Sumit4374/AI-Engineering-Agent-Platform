package sumit.ai.ai_engineering.ai.provider;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NvidiaNimModelProvider implements ModelProvider {

    private final ChatClient chatClient;
    private final String modelName;
    private final String apiKey;

    public NvidiaNimModelProvider(
            ChatClient chatClient,
            @Value("${spring.ai.openai.chat.options.model:meta/llama-3.1-70b-instruct}") String modelName,
            @Value("${spring.ai.openai.api-key:}") String apiKey) {
        this.chatClient = chatClient;
        this.modelName = modelName;
        this.apiKey = apiKey;
    }

    @Override
    public ModelProviderType getProviderType() {
        return ModelProviderType.NVIDIA_NIM;
    }

    @Override
    public String getProviderName() {
        return "NVIDIA NIM Cloud API";
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
