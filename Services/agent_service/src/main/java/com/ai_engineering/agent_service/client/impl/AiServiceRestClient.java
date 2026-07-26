package com.ai_engineering.agent_service.client.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.ai_engineering.agent_service.client.AiServicePort;
import com.ai_engineering.agent_service.client.DownstreamServiceException;
import com.ai_engineering.agent_service.client.dto.AiCapability;

/**
 * {@link AiServicePort} backed by a load-balanced {@link RestClient} calling
 * {@code ai_service} through Eureka, wrapped in a resilience4j circuit breaker.
 *
 * <p>Failure handling:
 * <ul>
 *   <li>A non-2xx from the AI service is surfaced with the <em>same</em> status
 *       (e.g. its 400 validation error passes through as our 400).</li>
 *   <li>Transport failures (AI service down, timeout) become 503.</li>
 *   <li>When the breaker is open, the fallback short-circuits to 503 without
 *       even attempting the call.</li>
 * </ul>
 */
@Component
public class AiServiceRestClient implements AiServicePort {

    private static final Logger log = LoggerFactory.getLogger(AiServiceRestClient.class);

    private static final ParameterizedTypeReference<Map<String, Object>> JSON_MAP =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final String aiServiceId;

    public AiServiceRestClient(RestClient.Builder loadBalancedRestClientBuilder,
                               CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                               @Value("${agent.downstream.ai-service:ai_service}") String aiServiceId) {
        this.restClient = loadBalancedRestClientBuilder.build();
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.aiServiceId = aiServiceId;
    }

    @Override
    public Map<String, Object> invoke(AiCapability capability, Map<String, Object> payload) {
        CircuitBreaker breaker = circuitBreakerFactory.create("ai-service");
        String uri = "http://" + aiServiceId + "/api/v1/ai/" + capability.path();

        return breaker.run(
                () -> call(uri, payload),
                throwable -> fallback(capability, throwable));
    }

    private Map<String, Object> call(String uri, Map<String, Object> payload) {
        try {
            return restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JSON_MAP);
        } catch (RestClientResponseException e) {
            // The AI service responded with a non-2xx status. Preserve it so
            // client validation errors (400) don't masquerade as our failures.
            throw new DownstreamServiceException(
                    e.getStatusCode(),
                    "AI service returned " + e.getStatusCode().value(),
                    e);
        }
    }

    private Map<String, Object> fallback(AiCapability capability, Throwable throwable) {
        // Reached on transport failure or an open breaker. If the cause already
        // carries a downstream status (a non-2xx), rethrow it unchanged.
        if (throwable instanceof DownstreamServiceException dse) {
            throw dse;
        }
        log.warn("AI service call failed for capability {}: {}", capability, throwable.getMessage());
        throw new DownstreamServiceException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "AI service is unavailable",
                throwable);
    }
}
