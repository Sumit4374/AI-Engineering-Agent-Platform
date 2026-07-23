package com.ai_engineering.ai_service.capability;

/**
 * Identifies every AI capability the service exposes. Each capability bean
 * declares which type it handles so the {@link CapabilityRegistry} can resolve
 * requests to the correct handler.
 */
public enum CapabilityType {
    CHAT,
    EXPLAIN,
    CODE_REVIEW,
    SUMMARIZATION,
    DEBUG,
    ARCHITECTURE,
    DOCUMENTATION,
    REQUIREMENT_ANALYSIS,
    API_EXPLANATION
}
