package com.ai_engineering.ai_service.Service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ai_engineering.ai_service.DTOs.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.DTOs.ChatDTO.ChatResponse;
import com.ai_engineering.ai_service.Engine.AIEngineImpl;
import com.ai_engineering.ai_service.Engine.Prompts.PromptType;

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
                ),
                String.class)
        );
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
