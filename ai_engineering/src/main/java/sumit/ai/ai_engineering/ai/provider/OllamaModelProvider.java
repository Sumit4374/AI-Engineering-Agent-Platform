package sumit.ai.ai_engineering.ai.provider;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OllamaModelProvider implements ModelProvider {

    private final ChatClient chatClient;
    private final String modelName;
    private final String baseUrl;

    public OllamaModelProvider(
            ChatClient chatClient,
            @Value("${ai.ollama.model:llama3}") String modelName,
            @Value("${ai.ollama.base-url:http://localhost:11434}") String baseUrl) {
        this.chatClient = chatClient;
        this.modelName = modelName;
        this.baseUrl = baseUrl;
    }

    @Override
    public ModelProviderType getProviderType() {
        return ModelProviderType.OLLAMA;
    }

    @Override
    public String getProviderName() {
        return "Ollama Local LLM";
    }

    @Override
    public String getDefaultModel() {
        return modelName;
    }

    @Override
    public boolean isAvailable() {
        return baseUrl != null && !baseUrl.isBlank();
    }

    @Override
    public ChatClient getChatClient() {
        return chatClient;
    }
}
