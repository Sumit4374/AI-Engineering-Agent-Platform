package com.ai_engineering.agent_service.orchestration;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ai_engineering.agent_service.client.AiServicePort;
import com.ai_engineering.agent_service.client.dto.AiCapability;
import com.ai_engineering.agent_service.security.AuthenticatedUser;

/**
 * Coordinates a single agent request end-to-end.
 *
 * <p>Today it does two things: enforces per-user conversation isolation and
 * forwards the call to the AI service. It is deliberately the one place where
 * future context-gathering (Memory history, RAG chunks, MCP tool results) will
 * be injected before the AI call — see the marked seam below — so that
 * responsibility never leaks into the controller or the HTTP client.
 */
@Service
public class AgentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrator.class);

    private final AiServicePort aiService;

    public AgentOrchestrator(AiServicePort aiService) {
        this.aiService = aiService;
    }

    /**
     * Runs one AI capability on behalf of an authenticated user.
     *
     * @param user       the validated caller (from the JWT)
     * @param capability which AI capability to invoke
     * @param request    the raw JSON body the client sent
     * @return the AI service's JSON response
     */
    public Map<String, Object> handle(AuthenticatedUser user,
                                      AiCapability capability,
                                      Map<String, Object> request) {

        // Copy so we never mutate the controller's inbound map.
        Map<String, Object> payload = new HashMap<>(request);

        // --- Conversation isolation -------------------------------------
        // The AI service persists chat memory keyed by conversationId and does
        // no isolation of its own. Namespacing the id with the authenticated
        // userId means one user can never address (or read) another user's
        // conversation, even if they guess the raw id.
        payload.put("conversationId", scopedConversationId(user, request.get("conversationId")));

        // --- Context-gathering seam -------------------------------------
        // FUTURE: fetch conversation history (Memory Service) and retrieve
        // relevant document chunks (RAG Service), then inline them into
        // `payload` here before the AI call. Not wired yet — those services
        // expose no endpoints. See ai_service/plan.md §6.

        log.debug("Dispatching capability={} for userId={}", capability, user.userId());
        return aiService.invoke(capability, payload);
    }

    /**
     * Namespaces the client-supplied conversation id under the user id. A
     * missing id yields a per-user default bucket rather than a shared/null one.
     */
    private String scopedConversationId(AuthenticatedUser user, Object rawConversationId) {
        String base = (rawConversationId == null || rawConversationId.toString().isBlank())
                ? "default"
                : rawConversationId.toString();
        return "u" + user.userId() + ":" + base;
    }
}
