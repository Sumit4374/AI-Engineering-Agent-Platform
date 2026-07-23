package com.ai_engineering.ai_service.capability.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.capability.AbstractTextCapability;
import com.ai_engineering.ai_service.capability.CapabilityType;
import com.ai_engineering.ai_service.dto.ApiExplanationDTO.ApiExplanationRequest;
import com.ai_engineering.ai_service.dto.ApiExplanationDTO.ApiExplanationResponse;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.PromptType;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class ApiExplanationCapability extends AbstractTextCapability<ApiExplanationRequest, ApiExplanationResponse> {

    public ApiExplanationCapability(AIEngine engine) {
        super(engine);
    }

    @Override
    public CapabilityType type() {
        return CapabilityType.API_EXPLANATION;
    }

    @Override
    protected String promptType() {
        return PromptType.API_EXPLANATION.getFileName();
    }

    @Override
    protected ToolsCategory[] tools() {
        return new ToolsCategory[]{ToolsCategory.DOCUMENTATION};
    }

    @Override
    protected Map<String, Object> variables(ApiExplanationRequest request) {
        return Map.of("api", request.api());
    }

    @Override
    protected ApiExplanationResponse map(String content) {
        return new ApiExplanationResponse(content);
    }
}
