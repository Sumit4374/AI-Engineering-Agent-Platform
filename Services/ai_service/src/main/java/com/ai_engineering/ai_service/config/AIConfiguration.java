package com.ai_engineering.ai_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.ai_engineering.ai_service.provider.ProviderProperties;

/**
 * Enables provider configuration binding. Chat clients are no longer defined as
 * a single bean here — each provider builds its own {@code ChatClient} through
 * the {@code ProviderFactory}, so the engine can select between providers at
 * runtime.
 */
@Configuration
@EnableConfigurationProperties(ProviderProperties.class)
public class AIConfiguration {
}
