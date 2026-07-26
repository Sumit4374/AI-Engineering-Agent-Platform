package com.ai_engineering.agent_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * HTTP client used for all downstream service-to-service calls.
 *
 * <p>The {@link LoadBalanced} {@link RestClient.Builder} lets callers use a
 * Eureka service id as the host (e.g. {@code http://ai_service/...}); Spring
 * Cloud LoadBalancer resolves it to a live instance. No hard-coded host/port.
 */
@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }
}
