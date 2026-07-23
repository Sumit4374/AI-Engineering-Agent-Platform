package com.ai_engineering.ai_service.capability.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.capability.AbstractStructuredCapability;
import com.ai_engineering.ai_service.capability.CapabilityType;
import com.ai_engineering.ai_service.dto.RequirementDTO.RequirementAnalysisResponse;
import com.ai_engineering.ai_service.dto.RequirementDTO.RequirementRequest;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.PromptType;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class RequirementAnalysisCapability extends AbstractStructuredCapability<RequirementRequest, RequirementAnalysisResponse> {

    public RequirementAnalysisCapability(AIEngine engine) {
        super(engine);
    }

    @Override
    public CapabilityType type() {
        return CapabilityType.REQUIREMENT_ANALYSIS;
    }

    @Override
    protected String promptType() {
        return PromptType.REQUIREMENT_ANALYSIS.getFileName();
    }

    @Override
    protected ToolsCategory[] tools() {
        return new ToolsCategory[]{};
    }

    @Override
    protected Map<String, Object> variables(RequirementRequest request) {
        return Map.of("text", request.text());
    }

    @Override
    protected Class<RequirementAnalysisResponse> responseType() {
        return RequirementAnalysisResponse.class;
    }
}
