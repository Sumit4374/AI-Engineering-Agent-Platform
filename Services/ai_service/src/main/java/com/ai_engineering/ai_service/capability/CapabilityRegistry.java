package com.ai_engineering.ai_service.capability;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Resolves an incoming request to the {@link Capability} bean that handles its
 * {@link CapabilityType} and executes it. This is the Goal's "Capability
 * Resolver" stage in the request flow.
 */
@Component
public class CapabilityRegistry {

    private final Map<CapabilityType, Capability<?, ?>> capabilities = new EnumMap<>(CapabilityType.class);

    public CapabilityRegistry(List<Capability<?, ?>> capabilityBeans) {
        for (Capability<?, ?> capability : capabilityBeans) {
            Capability<?, ?> existing = capabilities.put(capability.type(), capability);
            if (existing != null) {
                throw new IllegalStateException(
                    "Duplicate capability registered for type " + capability.type()
                    + ": " + existing.getClass().getName() + " and " + capability.getClass().getName());
            }
        }
    }

    /**
     * Execute the capability for {@code type} against {@code request}.
     *
     * @throws IllegalArgumentException if no capability handles the type
     */
    @SuppressWarnings("unchecked")
    public <REQ extends CapabilityRequest, RES> RES execute(CapabilityType type, REQ request) throws IOException {
        Capability<REQ, RES> capability = (Capability<REQ, RES>) capabilities.get(type);
        if (capability == null) {
            throw new IllegalArgumentException("No capability registered for type: " + type);
        }
        return capability.execute(request);
    }
}
