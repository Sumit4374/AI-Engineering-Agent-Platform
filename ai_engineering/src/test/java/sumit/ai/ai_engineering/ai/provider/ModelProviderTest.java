package sumit.ai.ai_engineering.ai.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

class ModelProviderTest {

    private ChatClient chatClient;
    private NvidiaNimModelProvider nvidiaProvider;
    private OpenAiModelProvider openAiProvider;
    private OllamaModelProvider ollamaProvider;
    private ModelProviderRegistry registry;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        nvidiaProvider = new NvidiaNimModelProvider(chatClient, "meta/llama-3.1-70b-instruct", "dummy-key");
        openAiProvider = new OpenAiModelProvider(chatClient, "gpt-4o-mini", "dummy-openai-key");
        ollamaProvider = new OllamaModelProvider(chatClient, "llama3", "http://localhost:11434");

        registry = new ModelProviderRegistry(
                List.of(nvidiaProvider, openAiProvider, ollamaProvider),
                "NVIDIA_NIM"
        );
    }

    @Test
    void nvidiaProvider_propertiesAndAvailability() {
        assertThat(nvidiaProvider.getProviderType()).isEqualTo(ModelProviderType.NVIDIA_NIM);
        assertThat(nvidiaProvider.getDefaultModel()).isEqualTo("meta/llama-3.1-70b-instruct");
        assertThat(nvidiaProvider.isAvailable()).isTrue();
        assertThat(nvidiaProvider.getChatClient()).isSameAs(chatClient);
    }

    @Test
    void openAiProvider_propertiesAndAvailability() {
        assertThat(openAiProvider.getProviderType()).isEqualTo(ModelProviderType.OPENAI);
        assertThat(openAiProvider.getDefaultModel()).isEqualTo("gpt-4o-mini");
        assertThat(openAiProvider.isAvailable()).isTrue();
    }

    @Test
    void ollamaProvider_propertiesAndAvailability() {
        assertThat(ollamaProvider.getProviderType()).isEqualTo(ModelProviderType.OLLAMA);
        assertThat(ollamaProvider.getDefaultModel()).isEqualTo("llama3");
        assertThat(ollamaProvider.isAvailable()).isTrue();
    }

    @Test
    void registry_resolvesConfiguredActiveProvider() {
        ModelProvider active = registry.getActiveProvider();
        assertThat(active).isNotNull();
        assertThat(active.getProviderType()).isEqualTo(ModelProviderType.NVIDIA_NIM);
    }

    @Test
    void registry_switchesActiveProviderDynamically() {
        registry.setActiveProvider(ModelProviderType.OPENAI);
        assertThat(registry.getActiveProviderType()).isEqualTo(ModelProviderType.OPENAI);
        assertThat(registry.getActiveProvider().getProviderType()).isEqualTo(ModelProviderType.OPENAI);
    }

    @Test
    void registry_getAllProviders_includesAllWithActiveFlag() {
        List<ModelProviderRegistry.ModelProviderInfo> all = registry.getAllProviders();
        assertThat(all).hasSize(3);
        assertThat(all.stream().filter(ModelProviderRegistry.ModelProviderInfo::active).count()).isEqualTo(1);
    }

    @Test
    void registry_unknownProvider_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> registry.setActiveProvider(ModelProviderType.MOCK))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
