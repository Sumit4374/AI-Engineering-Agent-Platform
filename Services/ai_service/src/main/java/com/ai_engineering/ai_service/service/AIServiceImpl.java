package com.ai_engineering.ai_service.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ai_engineering.ai_service.Capability.ChatCapability.ChatCapability;
import com.ai_engineering.ai_service.dto.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.dto.ChatDTO.ChatResponse;
import com.ai_engineering.ai_service.dto.CodeReviewDTO.CodeReviewRequest;
import com.ai_engineering.ai_service.dto.CodeReviewDTO.CodeReviewResponse;
import com.ai_engineering.ai_service.dto.ExplainDTO.ExplainRequest;
import com.ai_engineering.ai_service.dto.ExplainDTO.ExplainResponse;
import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeRequest;
import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeResponse;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.PromptType;
import com.ai_engineering.ai_service.prompt.SummaryTypeLoader;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Service
public class AIServiceImpl implements AIService {

    private final AIEngine engine;
    private final SummaryTypeLoader summaryTypeLoader;
    private final ChatCapability chatCapability;

    AIServiceImpl(AIEngine engine, SummaryTypeLoader summaryTypeLoader, ChatCapability chatCapability) {
        this.engine = engine;
        this.summaryTypeLoader = summaryTypeLoader;
        this.chatCapability = chatCapability;
    }

    private String getConversationId(String conversationId){
        if(conversationId==null || conversationId.isBlank()){
            return UUID.randomUUID().toString();
        }
        return conversationId;
    }

    @Override
    public ChatResponse chat(ChatRequest req) throws IOException{
        return chatCapability.execute(req);
    }

    @Override
    public ExplainResponse explain(ExplainRequest req) throws IOException {
        return new ExplainResponse(
            engine.generate(
                getConversationId(req.conversationId()),
                PromptType.EXPLAIN.getFileName(),
                Map.of(
                    "topic",req.topic()
                ),
                ToolsCategory.DOCUMENTATION
            )
        );
    }

    @Override
    public SummarizeResponse summarize(SummarizeRequest req) throws IOException {
        return engine.generateStructure(
                getConversationId(req.conversationId()),
                summaryTypeLoader.loadSummaryType(req.type()),
                Map.of(
                    "text",req.text()
                ),
                SummarizeResponse.class);
    }

    @Override
    public CodeReviewResponse codeReview(CodeReviewRequest req) throws IOException {
        return engine.generateStructure(
            getConversationId(req.conversationId()),
            PromptType.CODE_REVIEW.getFileName(),
            Map.of(
                "code",req.code()
            ),
            CodeReviewResponse.class,
            ToolsCategory.DEVELOPMENT);
    }

}
