package com.ai_engineering.ai_service.provider;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Provider-agnostic handle to one LLM backend. The rest of the application
 * (AI Engine, capabilities) talks only to this interface and never to a
 * specific vendor SDK — the Goal's "Provider Abstraction" principle:
 *
 * <pre>AI Engine → Provider Manager → Provider → LLM</pre>
 *
 * <p>Implementations wrap a configured {@link ChatClient}. Because Ollama,
 * NVIDIA NIM, Azure OpenAI and others expose an OpenAI-compatible API, a single
 * OpenAI-compatible implementation covers many backends via base-url config.
 * A native (non-OpenAI-wire) provider would simply be another implementation.
 */
public interface AiProvider {

    /** Unique logical name used for selection and fallback, e.g. "ollama". */
    String name();

    /** The model id this provider issues requests against. */
    String model();

    /** The configured chat client used to execute prompts. */
    ChatClient chatClient();

    /** Whether this provider may be chosen as a fallback target. */
    boolean fallbackEligible();

    /**
     * Actively probe the backend (a cheap call) and report health. Used for
     * monitoring and to skip dead providers during fallback.
     */
    ProviderHealth health();
}
