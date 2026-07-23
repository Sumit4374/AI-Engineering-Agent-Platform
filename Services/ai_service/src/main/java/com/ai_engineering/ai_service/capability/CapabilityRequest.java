package com.ai_engineering.ai_service.capability;

/**
 * Contract shared by every capability request DTO so the capability layer can
 * resolve the conversation id uniformly without knowing the concrete type.
 */
public interface CapabilityRequest {
    String conversationId();
}
