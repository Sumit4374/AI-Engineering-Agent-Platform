package com.ai_engineering.ai_service.capability.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.capability.AbstractStructuredCapability;
import com.ai_engineering.ai_service.capability.CapabilityType;
import com.ai_engineering.ai_service.dto.ArchitectureDTO.ArchitectureRequest;
import com.ai_engineering.ai_service.dto.ArchitectureDTO.ArchitectureReviewResponse;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.PromptType;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class ArchitectureCapability extends AbstractStructuredCapability<ArchitectureRequest, ArchitectureReviewResponse> {

    public ArchitectureCapability(AIEngine engine) {
        super(engine);
    }

    @Override
    public CapabilityType type() {
        return CapabilityType.ARCHITECTURE;
    }

    @Override
    protected String promptType() {
        return PromptType.ARCHITECTURE.getFileName();
    }

    @Override
    protected ToolsCategory[] tools() {
        return new ToolsCategory[]{ToolsCategory.DOCUMENTATION};
    }

    @Override
    protected Map<String, Object> variables(ArchitectureRequest request) {
        return Map.of("context", request.context());
    }

    @Override
    protected Class<ArchitectureReviewResponse> responseType() {
        return ArchitectureReviewResponse.class;
    }
}
