package com.ai_engineering.ai_service.capability;

/**
 * Contract shared by every capability request DTO so the capability layer can
 * resolve the conversation id (and optionally a target provider) uniformly
 * without knowing the concrete type.
 */
public interface CapabilityRequest {

    String conversationId();

    /**
     * Optional AI provider name to handle this request (e.g. "openai",
     * "ollama"). When null/blank the configured default provider is used.
     * Records that want per-request provider selection can override this by
     * declaring a matching component; others inherit the null default.
     */
    default String provider() {
        return null;
    }
}
