package sumit.ai.ai_engineering.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import sumit.ai.ai_engineering.infrastructure.observability.PlatformMetrics;

class PlatformMetricsTest {

    private MeterRegistry meterRegistry;
    private PlatformMetrics platformMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        platformMetrics = new PlatformMetrics(meterRegistry);
    }

    @Test
    void recordLlmCall_recordsRequestTimerAndTokens() {
        platformMetrics.recordLlmCall("NVIDIA_NIM", "EXPLAIN.prompt", 250, true, 120);

        assertThat(meterRegistry.find("ai.llm.requests").counter()).isNotNull();
        assertThat(meterRegistry.find("ai.llm.requests").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("ai.llm.duration").timer()).isNotNull();
        assertThat(meterRegistry.find("ai.llm.tokens").counter().count()).isEqualTo(120.0);
    }

    @Test
    void recordToolExecution_recordsToolMetrics() {
        platformMetrics.recordToolExecution("calculator", 15, true);

        assertThat(meterRegistry.find("ai.tool.executions").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("ai.tool.duration").timer()).isNotNull();
    }

    @Test
    void recordRagRetrieval_recordsRetrievalMetrics() {
        platformMetrics.recordRagRetrieval(5, 45, true);

        assertThat(meterRegistry.find("ai.rag.retrievals").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("ai.rag.chunks_retrieved").counter().count()).isEqualTo(5.0);
    }

    @Test
    void recordAgentExecution_recordsAgentMetrics() {
        platformMetrics.recordAgentExecution(1200, 3, true);

        assertThat(meterRegistry.find("ai.agent.executions").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("ai.agent.iterations").counter().count()).isEqualTo(3.0);
    }

    @Test
    void recordKafkaEvent_recordsKafkaMetrics() {
        platformMetrics.recordKafkaEvent("document.uploaded", true);

        assertThat(meterRegistry.find("ai.kafka.events").counter().count()).isEqualTo(1.0);
    }
}
