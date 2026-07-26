package com.ai_engineering.agent_service.client;

import org.springframework.http.HttpStatusCode;

/**
 * Raised when a downstream service call fails (transport error, non-2xx
 * response, or an open circuit breaker). Carries the status the agent should
 * surface to its own caller so the {@code GlobalExceptionHandler} can map it.
 */
public class DownstreamServiceException extends RuntimeException {

    private final HttpStatusCode status;

    public DownstreamServiceException(HttpStatusCode status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}
