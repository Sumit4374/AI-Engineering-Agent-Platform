package com.ai_engineering.agent_service.client;

/**
 * Seam for the future RAG Service.
 *
 * <p><b>Not yet implemented.</b> The {@code rag_service} project exists but
 * exposes no endpoints yet. When it lands, add an implementation that retrieves
 * relevant document chunks (by conversation-referenced doc ids) so the
 * orchestrator can inline them as context into AI requests. Per the platform
 * design, embeddings/documents are content-hash keyed and owned by RAG; the
 * agent only asks for retrieval, it does not store vectors.
 *
 * <p>Defined now purely to document the orchestration seam; it has no methods
 * until the contract is known.
 */
public interface RagServicePort {
    // Intentionally empty: placeholder for the RAG Service contract.
}
