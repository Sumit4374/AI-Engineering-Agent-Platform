package sumit.ai.ai_engineering.ai.Capability.SummaryCapability;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.Capability.IdResolver.CheckConversationId;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeRequest;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeResponse;
import sumit.ai.ai_engineering.ai.engine.AIEngine;
import sumit.ai.ai_engineering.ai.prompt.SummaryTypeLoader;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

/**
 * Default implementation of {@link SummaryCapability}.
 *
 * <p>Delegates to the {@link AIEngine} for both blocking structured-output
 * summarisation and streaming summarisation, using the prompt file resolved
 * by {@link SummaryTypeLoader} for the requested summary style.
 */
@Service
public class SummaryCapabilityImpl implements SummaryCapability {

    private final AIEngine engine;
    private final SummaryTypeLoader summaryTypeLoader;

    public SummaryCapabilityImpl(AIEngine engine, SummaryTypeLoader summaryTypeLoader) {
        this.engine = engine;
        this.summaryTypeLoader = summaryTypeLoader;
    }

    @Override
    public SummarizeResponse execute(SummarizeRequest request) throws IOException {
        return engine.generateStructure(
            CheckConversationId.check(request.conversationId()),
            summaryTypeLoader.loadSummaryType(request.type()),
            Map.of("text", request.text()),
            SummarizeResponse.class
        );
    }

    @Override
    public Flux<String> stream(SummarizeRequest request) throws IOException {
        return engine.stream(
            CheckConversationId.check(request.conversationId()),
            summaryTypeLoader.loadSummaryType(request.type()),
            Map.of("text", request.text()),
            ToolsCategory.UTILITY
        );
    }
}
