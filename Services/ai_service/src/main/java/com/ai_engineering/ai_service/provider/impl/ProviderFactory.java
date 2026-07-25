package com.ai_engineering.ai_service.provider.impl;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.setup.OpenAiSetup;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.provider.AiProvider;
import com.ai_engineering.ai_service.provider.ProviderProperties.Registration;
import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.credential.BearerTokenCredential;
import com.openai.credential.Credential;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;

/**
 * Builds a live {@link AiProvider} from a {@link Registration} using the same
 * construction path Spring AI's OpenAI autoconfiguration uses internally
 * ({@link OpenAiSetup#setupSyncClient} → {@link OpenAiChatModel} →
 * {@link ChatClient}). This keeps provider creation dependency-only on the
 * OpenAI starter that ships with the app, so every OpenAI-compatible backend
 * works without an extra starter jar.
 */
@Component
public class ProviderFactory {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(120);
    private static final int DEFAULT_MAX_RETRIES = 2;

    /** Bearer token used for key-less endpoints (e.g. local Ollama), which ignore it. */
    private static final String PLACEHOLDER_KEY = "not-needed";

    private final ChatMemory chatMemory;
    private final ObservationRegistry observationRegistry;
    private final MeterRegistry meterRegistry;

    public ProviderFactory(ChatMemory chatMemory,
                           ObjectProvider<ObservationRegistry> observationRegistry,
                           ObjectProvider<MeterRegistry> meterRegistry) {
        this.chatMemory = chatMemory;
        // The OpenAI SDK requires non-null registries; fall back to no-op
        // implementations when the app has not defined these beans.
        this.observationRegistry = observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP);
        this.meterRegistry = meterRegistry.getIfAvailable(SimpleMeterRegistry::new);
    }

    public AiProvider create(Registration reg) {
        // The OpenAI SDK requires a Credential to build a client; its String
        // apiKey argument is only used for provider detection, not auth. So we
        // always supply a BearerTokenCredential. Key-less endpoints (e.g. a
        // local Ollama runtime) ignore the Authorization header, so a
        // placeholder token is harmless there.
        String key = (reg.getApiKey() == null || reg.getApiKey().isBlank())
                ? PLACEHOLDER_KEY
                : reg.getApiKey();
        Credential credential = BearerTokenCredential.create(key);

        // OpenAiChatModel needs BOTH a sync and an async client. If only the
        // sync client is set, build() derives the async one via setupAsyncClient
        // with no credentials and fails, so we construct both explicitly.
        OpenAIClient syncClient = OpenAiSetup.setupSyncClient(
                reg.getBaseUrl(), key, credential,
                null, null, null, false, false, reg.getModel(),
                DEFAULT_TIMEOUT, DEFAULT_MAX_RETRIES, null, Map.of(),
                observationRegistry, meterRegistry, List.of());

        OpenAIClientAsync asyncClient = OpenAiSetup.setupAsyncClient(
                reg.getBaseUrl(), key, credential,
                null, null, null, false, false, reg.getModel(),
                DEFAULT_TIMEOUT, DEFAULT_MAX_RETRIES, null, Map.of(),
                observationRegistry, meterRegistry, List.of());

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder();
        // Call as statements: the builder mutates in place and its self-return
        // type is erased to a parent builder, so chaining onto the assignment
        // would not compile. build() on OpenAiChatOptions.Builder returns
        // OpenAiChatOptions.
        optionsBuilder.model(reg.getModel());
        if (reg.getTemperature() != null) {
            optionsBuilder.temperature(reg.getTemperature());
        }
        OpenAiChatOptions options = optionsBuilder.build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiClient(syncClient)
                .openAiClientAsync(asyncClient)
                .options(options)
                .observationRegistry(observationRegistry)
                .meterRegistry(meterRegistry)
                .build();

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();

        return new OpenAiCompatibleProvider(
                reg.getName(), reg.getModel(), reg.isFallbackEligible(), chatClient);
    }
}
