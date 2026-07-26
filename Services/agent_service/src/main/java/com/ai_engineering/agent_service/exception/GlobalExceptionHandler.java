package com.ai_engineering.agent_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ai_engineering.agent_service.client.DownstreamServiceException;

/**
 * Translates failures into RFC-7807 {@link ProblemDetail} responses, matching
 * the convention used by {@code auth_service} and {@code ai_service}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Unknown capability / bad path variable → 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Downstream call failed. Preserve a client-error status (e.g. the AI
     * service's 400 validation) as-is; collapse anything else to a 502/503-style
     * upstream failure carried on the exception.
     */
    @ExceptionHandler(DownstreamServiceException.class)
    public ProblemDetail handleDownstream(DownstreamServiceException ex) {
        HttpStatusCode status = ex.getStatus();
        if (status.is5xxServerError()) {
            log.warn("Downstream failure: {}", ex.getMessage());
        }
        return ProblemDetail.forStatusAndDetail(status, ex.getMessage());
    }

    /** Anything unexpected → 500, without leaking internals. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error handling agent request", ex);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}
