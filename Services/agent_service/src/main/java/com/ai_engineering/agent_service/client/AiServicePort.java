package com.ai_engineering.agent_service.client;

import java.util.Map;

import com.ai_engineering.agent_service.client.dto.AiCapability;

/**
 * Port to the downstream {@code ai_service} (the LLM execution layer).
 *
 * <p>The agent speaks to the AI service only through this interface, so the
 * orchestration logic never depends on HTTP/RestClient details or on which
 * transport is used. The payload/response are passed through as generic JSON
 * maps: the agent's job is to route + enrich context, not to re-model all nine
 * capability contracts (those live in {@code ai_service}).
 */
public interface AiServicePort {

    /**
     * Invokes an AI capability with an already-prepared payload (conversation
     * id and any context already injected by the orchestrator).
     *
     * @param capability which AI endpoint to call (allow-listed)
     * @param payload    the JSON request body to forward
     * @return the downstream JSON response as a map
     */
    Map<String, Object> invoke(AiCapability capability, Map<String, Object> payload);
}
