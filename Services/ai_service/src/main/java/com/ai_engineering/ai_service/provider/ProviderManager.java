package com.ai_engineering.ai_service.provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.provider.impl.ProviderFactory;

import jakarta.annotation.PostConstruct;

/**
 * Central registry and selector for AI providers — the Goal's "Provider
 * Manager" stage. It owns provider lifecycle (build from config at startup),
 * resolves which provider handles a request, and hides all vendor specifics
 * from the AI Engine.
 *
 * <p>Selection rules:
 * <ul>
 *   <li>An explicit provider name on the request wins (error if unknown).</li>
 *   <li>Otherwise the configured default provider is used.</li>
 * </ul>
 */
@Component
public class ProviderManager {

    private static final Logger log = LoggerFactory.getLogger(ProviderManager.class);

    private final ProviderProperties properties;
    private final ProviderFactory factory;

    /** Insertion-ordered so fallback follows configuration order. */
    private final Map<String, AiProvider> providers = new LinkedHashMap<>();
    private String defaultProvider;

    public ProviderManager(ProviderProperties properties, ProviderFactory factory) {
        this.properties = properties;
        this.factory = factory;
    }

    @PostConstruct
    void init() {
        for (ProviderProperties.Registration reg : properties.getRegistrations()) {
            if (!reg.isEnabled()) {
                log.info("Skipping disabled AI provider '{}'", reg.getName());
                continue;
            }
            validate(reg);
            providers.put(reg.getName(), factory.create(reg));
            log.info("Registered AI provider '{}' (model={}, baseUrl={})",
                    reg.getName(), reg.getModel(), reg.getBaseUrl());
        }

        if (providers.isEmpty()) {
            throw new IllegalStateException(
                "No AI providers configured. Define at least one under ai.providers.registrations[*].");
        }

        this.defaultProvider = resolveDefault();
        log.info("Default AI provider is '{}'", defaultProvider);
    }

    private void validate(ProviderProperties.Registration reg) {
        if (reg.getName() == null || reg.getName().isBlank()) {
            throw new IllegalStateException("A provider registration is missing 'name'.");
        }
        if (reg.getBaseUrl() == null || reg.getBaseUrl().isBlank()) {
            throw new IllegalStateException("Provider '" + reg.getName() + "' is missing 'base-url'.");
        }
        if (reg.getModel() == null || reg.getModel().isBlank()) {
            throw new IllegalStateException("Provider '" + reg.getName() + "' is missing 'model'.");
        }
        if (providers.containsKey(reg.getName())) {
            throw new IllegalStateException("Duplicate provider name: " + reg.getName());
        }
    }

    private String resolveDefault() {
        String configured = properties.getDefaultProvider();
        if (configured != null && !configured.isBlank()) {
            if (!providers.containsKey(configured)) {
                throw new IllegalStateException(
                    "ai.providers.default='" + configured + "' does not match any registered provider.");
            }
            return configured;
        }
        // Fall back to the first registered provider.
        return providers.keySet().iterator().next();
    }

    /** The provider to use when the request does not name one. */
    public AiProvider getDefault() {
        return providers.get(defaultProvider);
    }

    /**
     * Resolve a provider by name, or the default when {@code name} is null/blank.
     *
     * @throws IllegalArgumentException if a non-blank name matches no provider
     */
    public AiProvider resolve(String name) {
        if (name == null || name.isBlank()) {
            return getDefault();
        }
        AiProvider provider = providers.get(name);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown AI provider: '" + name
                    + "'. Available: " + providers.keySet());
        }
        return provider;
    }

    /**
     * Ordered fallback candidates after the primary: the primary first, then
     * every other fallback-eligible provider in configuration order. Used by
     * the engine to retry a failed generation on another backend.
     */
    public List<AiProvider> fallbackChain(AiProvider primary) {
        List<AiProvider> chain = new ArrayList<>();
        chain.add(primary);
        for (AiProvider p : providers.values()) {
            if (!p.name().equals(primary.name()) && p.fallbackEligible()) {
                chain.add(p);
            }
        }
        return chain;
    }

    /** Active health of every registered provider. */
    public List<ProviderHealth> healthAll() {
        List<ProviderHealth> out = new ArrayList<>();
        for (AiProvider p : providers.values()) {
            out.add(p.health());
        }
        return out;
    }

    public List<String> providerNames() {
        return new ArrayList<>(providers.keySet());
    }
}
