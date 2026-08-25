package sumit.ai.ai_engineering.ai.provider;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ModelProviderRegistry {

    private static final Logger log = LoggerFactory.getLogger(ModelProviderRegistry.class);

    private final Map<ModelProviderType, ModelProvider> providerMap = new EnumMap<>(ModelProviderType.class);
    private volatile ModelProviderType activeProviderType;

    public ModelProviderRegistry(
            List<ModelProvider> providers,
            @Value("${ai.provider.active:NVIDIA_NIM}") String configuredActive) {
        if (providers != null) {
            for (ModelProvider provider : providers) {
                providerMap.put(provider.getProviderType(), provider);
                log.info("Registered model provider: [type={}, name={}, defaultModel={}]",
                        provider.getProviderType(), provider.getProviderName(), provider.getDefaultModel());
            }
        }

        try {
            this.activeProviderType = ModelProviderType.valueOf(configuredActive.toUpperCase());
        } catch (Exception e) {
            this.activeProviderType = ModelProviderType.NVIDIA_NIM;
        }

        log.info("Active AI model provider set to: [{}]", this.activeProviderType);
    }

    public ModelProvider getActiveProvider() {
        ModelProvider provider = providerMap.get(activeProviderType);
        if (provider != null && provider.isAvailable()) {
            return provider;
        }

        // Fallback to first available provider
        for (ModelProvider p : providerMap.values()) {
            if (p.isAvailable()) {
                log.warn("Active provider [{}] unavailable, falling back to [{}]", activeProviderType, p.getProviderType());
                return p;
            }
        }

        // Return current provider even if offline (caller handles offline response gracefully)
        return provider != null ? provider : providerMap.values().stream().findFirst().orElse(null);
    }

    public void setActiveProvider(ModelProviderType type) {
        if (type == null || !providerMap.containsKey(type)) {
            throw new IllegalArgumentException("Unknown or unsupported model provider: " + type);
        }
        log.info("Switched active AI model provider from [{}] to [{}]", this.activeProviderType, type);
        this.activeProviderType = type;
    }

    public ModelProviderType getActiveProviderType() {
        return activeProviderType;
    }

    public List<ModelProviderInfo> getAllProviders() {
        return providerMap.values().stream()
                .map(p -> new ModelProviderInfo(
                        p.getProviderType().name(),
                        p.getProviderName(),
                        p.getDefaultModel(),
                        p.isAvailable(),
                        p.getProviderType() == activeProviderType
                ))
                .toList();
    }

    public record ModelProviderInfo(
            String type,
            String name,
            String defaultModel,
            boolean available,
            boolean active
    ) {}
}
