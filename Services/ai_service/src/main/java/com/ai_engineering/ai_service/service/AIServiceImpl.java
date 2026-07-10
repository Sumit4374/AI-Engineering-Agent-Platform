package com.ai_engineering.ai_service.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ai_engineering.ai_service.dto.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.dto.ChatDTO.ChatResponse;
import com.ai_engineering.ai_service.dto.ExplainDTO.ExplainRequest;
import com.ai_engineering.ai_service.dto.ExplainDTO.ExplainResponse;
import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeRequest;
import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeResponse;
import com.ai_engineering.ai_service.engine.AIEngineImpl;
import com.ai_engineering.ai_service.prompt.PromptType;

@Service
public class AIServiceImpl implements AIService {

    private final AIEngineImpl engine;

    AIServiceImpl(AIEngineImpl engine) {
        this.engine = engine;
    }

    @Override
    public ChatResponse chat(ChatRequest req) throws IOException{
        return new ChatResponse(
            engine.generate(
                PromptType.CHAT, 
                Map.of(
                    "question",req.request()
                )
            )
        );
    }

    @Override
    public ExplainResponse explain(ExplainRequest req) throws IOException {
        return new ExplainResponse(
            engine.generate(PromptType.EXPLAIN, 
                Map.of(
                    "topic",req.topic()
                )
            )
        );
    }

    @Override
    public SummarizeResponse summarize(SummarizeRequest req) throws IOException {
        return engine.generateStructure(
                PromptType.SUMMARIZATION, 
                Map.of(
                    "content",req.content()
                ), 
        SummarizeResponse.class);
    }

   

    // @Override
    // public CodeReviewResponse codeReview(CodeReviewRequest req) {
    //     String response =  chatClient
    //             .prompt()
    //             .call()
    //             .content();
    //     return new CodeReviewResponse(response, null, null);
    // }

    
    
}
