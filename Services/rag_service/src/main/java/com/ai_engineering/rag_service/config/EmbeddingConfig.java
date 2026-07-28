package com.ai_engineering.rag_service.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.setup.OpenAiSetup;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.openai.client.OpenAIClient;
import com.openai.credential.BearerTokenCredential;
import com.openai.credential.Credential;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;

/**
 * Builds the {@link EmbeddingModel} from {@link EmbeddingProperties} using the
 * same OpenAI client construction path {@code ai_service} uses for chat
 * ({@code OpenAiSetup#setupSyncClient} → {@code OpenAiEmbeddingModel}). This
 * keeps every OpenAI-compatible embedding backend (OpenAI, Ollama, etc.)
 * reachable through a base-url swap with no code change.
 *
 * <p>The pgvector {@code VectorStore} bean is auto-configured by the starter
 * and consumes this {@link EmbeddingModel} automatically.
 */
@Configuration
@EnableConfigurationProperties(EmbeddingProperties.class)
public class EmbeddingConfig {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(120);
    private static final int DEFAULT_MAX_RETRIES = 2;

    /** Dimension for text-embedding-3-small; must match the pgvector column. */
    private static final int EMBEDDING_DIMENSIONS = 1536;

    /** Bearer token used for key-less endpoints (e.g. local Ollama), which ignore it. */
    private static final String PLACEHOLDER_KEY = "not-needed";

    @Bean
    @Primary
    public EmbeddingModel embeddingModel(EmbeddingProperties props,
                                         ObjectProvider<ObservationRegistry> observationRegistry,
                                         ObjectProvider<MeterRegistry> meterRegistry) {
        // The OpenAI SDK requires non-null registries; fall back to no-ops.
        ObservationRegistry obs = observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP);
        MeterRegistry meter = meterRegistry.getIfAvailable(SimpleMeterRegistry::new);

        // A Credential is always required to build the client; the String key
        // arg is only used for provider detection. Key-less local endpoints
        // ignore the Authorization header, so a placeholder is harmless.
        String key = (props.getApiKey() == null || props.getApiKey().isBlank())
                ? PLACEHOLDER_KEY
                : props.getApiKey();
        Credential credential = BearerTokenCredential.create(key);

        OpenAIClient client = OpenAiSetup.setupSyncClient(
                props.getBaseUrl(), key, credential,
                null, null, null, false, false, props.getModel(),
                DEFAULT_TIMEOUT, DEFAULT_MAX_RETRIES, null, Map.of(),
                obs, meter, List.of());

        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(props.getModel())
                .dimensions(EMBEDDING_DIMENSIONS)
                .build();

        return OpenAiEmbeddingModel.builder()
                .openAiClient(client)
                .options(options)
                .observationRegistry(obs)
                .build();
    }
}
