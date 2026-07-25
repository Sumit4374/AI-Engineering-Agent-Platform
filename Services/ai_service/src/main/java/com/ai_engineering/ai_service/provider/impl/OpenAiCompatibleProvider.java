package com.ai_engineering.ai_service.provider.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

import com.ai_engineering.ai_service.provider.AiProvider;
import com.ai_engineering.ai_service.provider.ProviderHealth;

/**
 * Provider backed by any OpenAI-compatible chat-completions endpoint
 * (OpenAI, Ollama, NVIDIA NIM, Azure OpenAI, vLLM, OpenRouter, …).
 *
 * <p>The concrete {@link ChatClient} is constructed by
 * {@code ProviderFactory} and injected here; this class only adds identity,
 * fallback policy, and an active health probe.
 */
public class OpenAiCompatibleProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleProvider.class);

    private final String name;
    private final String model;
    private final boolean fallbackEligible;
    private final ChatClient chatClient;

    public OpenAiCompatibleProvider(String name, String model, boolean fallbackEligible, ChatClient chatClient) {
        this.name = name;
        this.model = model;
        this.fallbackEligible = fallbackEligible;
        this.chatClient = chatClient;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String model() {
        return model;
    }

    @Override
    public ChatClient chatClient() {
        return chatClient;
    }

    @Override
    public boolean fallbackEligible() {
        return fallbackEligible;
    }

    @Override
    public ProviderHealth health() {
        try {
            // Cheapest meaningful probe: a tiny prompt with no memory advisor.
            // Any exception (connection refused, auth, model missing) => DOWN.
            // A conversation id is required because the memory advisor is a
            // default advisor on every client; use an ephemeral one.
            String reply = chatClient.prompt()
                    .user("ping")
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "health-probe"))
                    .call()
                    .content();
            if (reply == null || reply.isBlank()) {
                return ProviderHealth.down(name, model, "Empty response from provider");
            }
            return ProviderHealth.up(name, model);
        } catch (Exception e) {
            log.debug("Health probe failed for provider {} ({}): {}", name, model, e.getMessage());
            return ProviderHealth.down(name, model, e.getMessage());
        }
    }
}
