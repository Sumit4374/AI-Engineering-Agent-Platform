package com.ai_engineering.ai_service.capability.impl;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.capability.Capability;
import com.ai_engineering.ai_service.capability.CapabilityType;
import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeRequest;
import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeResponse;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.SummaryTypeLoader;

/**
 * Summarization resolves its prompt dynamically from the requested
 * {@link com.ai_engineering.ai_service.prompt.SummaryType}, so it implements
 * {@link Capability} directly rather than extending a fixed-prompt base.
 */
@Component
public class SummarizationCapability implements Capability<SummarizeRequest, SummarizeResponse> {

    private final AIEngine engine;
    private final SummaryTypeLoader summaryTypeLoader;

    public SummarizationCapability(AIEngine engine, SummaryTypeLoader summaryTypeLoader) {
        this.engine = engine;
        this.summaryTypeLoader = summaryTypeLoader;
    }

    @Override
    public CapabilityType type() {
        return CapabilityType.SUMMARIZATION;
    }

    @Override
    public SummarizeResponse execute(SummarizeRequest request) throws IOException {
        String conversationId = request.conversationId();
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }
        return engine.generateStructure(
                conversationId,
                summaryTypeLoader.loadSummaryType(request.type()),
                Map.of("text", request.text()),
                SummarizeResponse.class);
    }
}
