package com.ai_engineering.ai_service.capability.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.capability.AbstractTextCapability;
import com.ai_engineering.ai_service.capability.CapabilityType;
import com.ai_engineering.ai_service.dto.ExplainDTO.ExplainRequest;
import com.ai_engineering.ai_service.dto.ExplainDTO.ExplainResponse;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.PromptType;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class ExplainCapability extends AbstractTextCapability<ExplainRequest, ExplainResponse> {

    public ExplainCapability(AIEngine engine) {
        super(engine);
    }

    @Override
    public CapabilityType type() {
        return CapabilityType.EXPLAIN;
    }

    @Override
    protected String promptType() {
        return PromptType.EXPLAIN.getFileName();
    }

    @Override
    protected ToolsCategory[] tools() {
        return new ToolsCategory[]{ToolsCategory.DOCUMENTATION};
    }

    @Override
    protected Map<String, Object> variables(ExplainRequest request) {
        return Map.of("topic", request.topic());
    }

    @Override
    protected ExplainResponse map(String content) {
        return new ExplainResponse(content);
    }
}
