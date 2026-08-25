package com.ai_engineering.ai_service.Capability.CodeReviewCapability;

import java.io.IOException;
import java.util.Map;

import com.ai_engineering.ai_service.Capability.IdResolver.CheckConversationId;
import com.ai_engineering.ai_service.dto.CodeReviewDTO.CodeReviewRequest;
import com.ai_engineering.ai_service.dto.CodeReviewDTO.CodeReviewResponse;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.PromptType;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

public class CodeReviewCapabilityImpl implements CodeReviewCapability{
    
    private final AIEngine engine;

    CodeReviewCapabilityImpl(AIEngine engine){
        this.engine = engine;
    }

    

    @Override
    public CodeReviewResponse execute(CodeReviewRequest request) throws IOException {
        return engine.generateStructure(
            CheckConversationId.check(request.conversationId()),
            PromptType.CODE_REVIEW.getFileName(),
            Map.of(
                "code",
                request.code()
            ), 
            CodeReviewResponse.class,
            ToolsCategory.DEVELOPMENT, ToolsCategory.DOCUMENTATION, ToolsCategory.UTILITY
        );
    }
}
