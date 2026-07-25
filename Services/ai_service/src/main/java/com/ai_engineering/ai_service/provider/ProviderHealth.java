package com.ai_engineering.ai_service.provider;

/**
 * Point-in-time health of a provider, surfaced via the ProviderManager and the
 * actuator health contributor.
 *
 * @param name       the provider's logical name
 * @param model      the model id the provider is configured to use
 * @param up         whether the last health probe succeeded
 * @param detail     human-readable status or error detail
 */
public record ProviderHealth(String name, String model, boolean up, String detail) {

    public static ProviderHealth up(String name, String model) {
        return new ProviderHealth(name, model, true, "Reachable");
    }

    public static ProviderHealth down(String name, String model, String detail) {
        return new ProviderHealth(name, model, false, detail);
    }
}
