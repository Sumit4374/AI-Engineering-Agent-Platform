package com.ai_engineering.ai_service.capability.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.capability.AbstractStructuredCapability;
import com.ai_engineering.ai_service.capability.CapabilityType;
import com.ai_engineering.ai_service.dto.CodeReviewDTO.CodeReviewRequest;
import com.ai_engineering.ai_service.dto.CodeReviewDTO.CodeReviewResponse;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.PromptType;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class CodeReviewCapability extends AbstractStructuredCapability<CodeReviewRequest, CodeReviewResponse> {

    public CodeReviewCapability(AIEngine engine) {
        super(engine);
    }

    @Override
    public CapabilityType type() {
        return CapabilityType.CODE_REVIEW;
    }

    @Override
    protected String promptType() {
        return PromptType.CODE_REVIEW.getFileName();
    }

    @Override
    protected ToolsCategory[] tools() {
        return new ToolsCategory[]{ToolsCategory.DEVELOPMENT};
    }

    @Override
    protected Map<String, Object> variables(CodeReviewRequest request) {
        return Map.of("code", request.code());
    }

    @Override
    protected Class<CodeReviewResponse> responseType() {
        return CodeReviewResponse.class;
    }
}
