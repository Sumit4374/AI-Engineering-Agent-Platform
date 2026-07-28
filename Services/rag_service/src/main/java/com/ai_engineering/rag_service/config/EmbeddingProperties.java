package com.ai_engineering.rag_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code rag.embedding.*} — the OpenAI-compatible embedding backend used
 * to vectorize documents and queries.
 *
 * <p>Default targets OpenAI ({@code text-embedding-3-small}); pointing
 * {@code base-url}/{@code api-key}/{@code model} at a local Ollama runtime
 * ({@code nomic-embed-text}) switches backend with no code change. Note the
 * embedding dimension must match {@code spring.ai.vectorstore.pgvector.dimensions}.
 */
@ConfigurationProperties(prefix = "rag.embedding")
public class EmbeddingProperties {

    /** OpenAI-compatible base URL, e.g. https://api.openai.com/v1 */
    private String baseUrl = "https://api.openai.com/v1";

    /** API key; ignored by key-less local endpoints (a placeholder is sent). */
    private String apiKey;

    /** Embedding model id, e.g. text-embedding-3-small. */
    private String model = "text-embedding-3-small";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
