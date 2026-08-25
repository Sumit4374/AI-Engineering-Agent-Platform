package sumit.ai.ai_engineering.infrastructure.observability;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Production-grade custom Micrometer metrics for LLM, Tools, RAG, Agent, and Kafka operations.
 */
@Component
public class PlatformMetrics {

    private final MeterRegistry meterRegistry;

    public PlatformMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordLlmCall(String provider, String promptType, long durationMs, boolean success, int estimatedTokens) {
        String status = success ? "SUCCESS" : "ERROR";
        String prov = provider != null ? provider : "UNKNOWN";
        String prompt = promptType != null ? promptType : "UNKNOWN";

        Counter.builder("ai.llm.requests")
                .tag("provider", prov)
                .tag("prompt", prompt)
                .tag("status", status)
                .description("Total LLM generation requests")
                .register(meterRegistry)
                .increment();

        Timer.builder("ai.llm.duration")
                .tag("provider", prov)
                .tag("prompt", prompt)
                .description("Latency of LLM generation calls")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        if (estimatedTokens > 0) {
            Counter.builder("ai.llm.tokens")
                    .tag("provider", prov)
                    .description("Estimated token throughput")
                    .register(meterRegistry)
                    .increment(estimatedTokens);
        }
    }

    public void recordToolExecution(String toolName, long durationMs, boolean success) {
        String status = success ? "SUCCESS" : "ERROR";
        String tool = toolName != null ? toolName : "UNKNOWN";

        Counter.builder("ai.tool.executions")
                .tag("tool", tool)
                .tag("status", status)
                .description("Total platform tool invocations")
                .register(meterRegistry)
                .increment();

        Timer.builder("ai.tool.duration")
                .tag("tool", tool)
                .description("Latency of tool executions")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordRagRetrieval(int chunksFound, long durationMs, boolean success) {
        String status = success ? "SUCCESS" : "ERROR";

        Counter.builder("ai.rag.retrievals")
                .tag("status", status)
                .description("Total RAG vector similarity searches")
                .register(meterRegistry)
                .increment();

        Timer.builder("ai.rag.duration")
                .description("Latency of RAG vector retrieval")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        Counter.builder("ai.rag.chunks_retrieved")
                .description("Total number of chunks retrieved by RAG queries")
                .register(meterRegistry)
                .increment(chunksFound);
    }

    public void recordAgentExecution(long durationMs, int iterations, boolean success) {
        String status = success ? "SUCCESS" : "ERROR";

        Counter.builder("ai.agent.executions")
                .tag("status", status)
                .description("Total autonomous agent executions")
                .register(meterRegistry)
                .increment();

        Timer.builder("ai.agent.duration")
                .description("Latency of agent goal executions")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        Counter.builder("ai.agent.iterations")
                .description("Total step iterations executed across all agents")
                .register(meterRegistry)
                .increment(iterations);
    }

    public void recordKafkaEvent(String topic, boolean success) {
        String status = success ? "SUCCESS" : "ERROR";
        String top = topic != null ? topic : "UNKNOWN";

        Counter.builder("ai.kafka.events")
                .tag("topic", top)
                .tag("status", status)
                .description("Total Kafka domain events dispatched")
                .register(meterRegistry)
                .increment();
    }
}
