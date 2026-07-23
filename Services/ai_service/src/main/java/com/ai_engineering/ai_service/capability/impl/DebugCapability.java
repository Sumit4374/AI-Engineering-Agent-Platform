package com.ai_engineering.ai_service.capability.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.capability.AbstractStructuredCapability;
import com.ai_engineering.ai_service.capability.CapabilityType;
import com.ai_engineering.ai_service.dto.DebugDTO.DebugRequest;
import com.ai_engineering.ai_service.dto.DebugDTO.DebugResponse;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.PromptType;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class DebugCapability extends AbstractStructuredCapability<DebugRequest, DebugResponse> {

    public DebugCapability(AIEngine engine) {
        super(engine);
    }

    @Override
    public CapabilityType type() {
        return CapabilityType.DEBUG;
    }

    @Override
    protected String promptType() {
        return PromptType.DEBUG.getFileName();
    }

    @Override
    protected ToolsCategory[] tools() {
        return new ToolsCategory[]{ToolsCategory.UTILITY, ToolsCategory.DEVELOPMENT};
    }

    @Override
    protected Map<String, Object> variables(DebugRequest request) {
        String stackTrace = request.stackTrace() == null ? "" : request.stackTrace();
        return Map.of(
            "code", request.code(),
            "stackTrace", stackTrace);
    }

    @Override
    protected Class<DebugResponse> responseType() {
        return DebugResponse.class;
    }
}
