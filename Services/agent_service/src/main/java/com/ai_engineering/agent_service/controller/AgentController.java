package com.ai_engineering.agent_service.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai_engineering.agent_service.client.dto.AiCapability;
import com.ai_engineering.agent_service.orchestration.AgentOrchestrator;
import com.ai_engineering.agent_service.security.AuthenticatedUser;

/**
 * Public entry point for agent capabilities. Every request is authenticated by
 * {@code JwtAuthenticationFilter}; the {@link AuthenticatedUser} is injected as
 * the security principal.
 *
 * <p>A single generic endpoint fronts all nine AI capabilities. The
 * {@code capability} path variable is resolved against the {@link AiCapability}
 * allow-list, so callers can only reach known downstream endpoints. Field-level
 * validation stays in {@code ai_service}, which owns each capability's contract
 * and returns a 400 the agent passes straight through.
 */
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final AgentOrchestrator orchestrator;

    public AgentController(AgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/{capability}")
    public ResponseEntity<Map<String, Object>> invoke(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String capability,
            @RequestBody(required = false) Map<String, Object> body) {

        AiCapability resolved = AiCapability.from(capability);
        Map<String, Object> request = body == null ? Map.of() : body;
        return ResponseEntity.ok(orchestrator.handle(user, resolved, request));
    }
}
