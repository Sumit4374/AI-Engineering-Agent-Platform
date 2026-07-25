package com.ai_engineering.ai_service.provider;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code ai.providers.*} configuration into a list of provider
 * definitions. Each entry describes one OpenAI-compatible endpoint (OpenAI,
 * Ollama, NVIDIA NIM, Azure OpenAI, vLLM, …) that the {@link ProviderManager}
 * turns into a live provider at startup.
 *
 * <p>Example (application.properties):
 * <pre>
 * ai.providers.default=openai
 * ai.providers.registrations[0].name=openai
 * ai.providers.registrations[0].base-url=${OPEN_API_BASE_URL}
 * ai.providers.registrations[0].api-key=${OPEN_API_KEY}
 * ai.providers.registrations[0].model=${CHAT_MODEL}
 * ai.providers.registrations[1].name=ollama
 * ai.providers.registrations[1].base-url=http://localhost:11434/v1
 * ai.providers.registrations[1].api-key=ollama
 * ai.providers.registrations[1].model=qwen2.5-coder:7b
 * </pre>
 */
@ConfigurationProperties(prefix = "ai.providers")
public class ProviderProperties {

    /** Name of the provider to use when a request does not specify one. */
    private String defaultProvider;

    private List<Registration> registrations = new ArrayList<>();

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }

    /** A single provider endpoint definition. */
    public static class Registration {

        /** Unique logical name, e.g. "openai" or "ollama". */
        private String name;

        /** OpenAI-compatible base URL, including the {@code /v1} suffix. */
        private String baseUrl;

        /**
         * API key. For key-less local endpoints (e.g. Ollama) any non-blank
         * placeholder works; the value is ignored by the server.
         */
        private String apiKey;

        /** Model id to request from this provider. */
        private String model;

        /** Sampling temperature; null lets the model/server default apply. */
        private Double temperature;

        /** Whether this provider participates in the registry. */
        private boolean enabled = true;

        /**
         * Whether this provider may be used as a fallback target. Defaults to
         * true; set false to keep a provider usable only when explicitly named.
         */
        private boolean fallbackEligible = true;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

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

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isFallbackEligible() {
            return fallbackEligible;
        }

        public void setFallbackEligible(boolean fallbackEligible) {
            this.fallbackEligible = fallbackEligible;
        }
    }
}
