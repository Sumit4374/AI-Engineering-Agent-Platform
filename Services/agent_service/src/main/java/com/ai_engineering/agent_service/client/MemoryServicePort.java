package com.ai_engineering.agent_service.client;

/**
 * Seam for the future Memory Service.
 *
 * <p><b>Not yet implemented.</b> The {@code memory_service} project exists but
 * exposes no endpoints yet, so there is intentionally no client bean. When it
 * lands, add an implementation that (per the platform design) fetches/stores
 * conversation history and cached artifacts, and wire it into
 * {@code AgentOrchestrator} so history is retrieved here and passed to the AI
 * service as inlined context — moving conversation ownership out of the AI
 * service (see ai_service/plan.md §6).
 *
 * <p>Defined now purely to document the orchestration seam; it has no methods
 * until the contract is known.
 */
public interface MemoryServicePort {
    // Intentionally empty: placeholder for the Memory Service contract.
}
