
package com.ai_engineering.agent_service.client.dto;

/**
 * The set of AI capabilities the agent can route to, mapped to the downstream
 * {@code ai_service} path segment under {@code /api/v1/ai/}.
 *
 * <p>This is an <b>allow-list</b>: the controller accepts a capability only if
 * it resolves to one of these constants, so a caller cannot make the agent POST
 * to an arbitrary downstream path.
 */
public enum AiCapability {

    CHAT("chat"),
    REVIEW("review"),
    EXPLAIN("explain"),
    SUMMARIZE("summarize"),
    DEBUG("debug"),
    ARCHITECTURE("architecture"),
    DOCUMENTATION("documentation"),
    REQUIREMENTS("requirements"),
    API_EXPLANATION("api-explanation");

    private final String path;

    AiCapability(String path) {
        this.path = path;
    }

    /** Downstream path segment, e.g. {@code "api-explanation"}. */
    public String path() {
        return path;
    }

    /**
     * Resolves a case-insensitive external token (path variable) to a
     * capability, accepting either the enum name or the path form.
     *
     * @throws IllegalArgumentException if no capability matches (surfaced as 400).
     */
    public static AiCapability from(String token) {
        if (token != null) {
            String normalized = token.trim().toLowerCase().replace('_', '-');
            for (AiCapability c : values()) {
                if (c.path.equals(normalized)) {
                    return c;
                }
            }
        }
        throw new IllegalArgumentException("Unknown AI capability: '" + token + "'");
    }
}
