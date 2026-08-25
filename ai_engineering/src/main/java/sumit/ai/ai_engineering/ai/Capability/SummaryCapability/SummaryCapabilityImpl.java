package com.ai_engineering.ai_service.Capability.SummaryCapability;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeRequest;
import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeResponse;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.PromptType;
import com.ai_engineering.ai_service.prompt.SummaryTypeLoader;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

import reactor.core.publisher.Flux;

@Service
public class SummaryCapabilityImpl implements SummaryCapability{

    private final AIEngine engine;
    private final SummaryTypeLoader summaryTypeLoader;

    private String getConversationId(String conversationId){
        if(conversationId==null || conversationId.isBlank()){
            return UUID.randomUUID().toString();
        }
        return conversationId;
    }

    SummaryCapabilityImpl(AIEngine engine, SummaryTypeLoader summaryTypeLoader){
        this.engine = engine;
        this.summaryTypeLoader = summaryTypeLoader;
    }

    @Override
    public SummarizeResponse execute(SummarizeRequest request) throws IOException {
        return engine.generateStructure(
            getConversationId(request.conversationId()),
            summaryTypeLoader.loadSummaryType(request.type()),
            Map.of(
                "text",request.text()
            ),
            SummarizeResponse.class
        );
    }
    @Override
    public Flux<String> stream(SummarizeRequest request) throws IOException {
        return engine.stream(
            getConversationId(request.conversationId()),
            PromptType.SUMMARIZATION.getFileName(),
            Map.of(
                "content",
                request.text()
            ), 
            ToolsCategory.UTILITY
        );
    }
}
