package com.ai_engineering.ai_service.capability.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.capability.AbstractStructuredCapability;
import com.ai_engineering.ai_service.capability.CapabilityType;
import com.ai_engineering.ai_service.dto.DocumentationDTO.DocumentationRequest;
import com.ai_engineering.ai_service.dto.DocumentationDTO.DocumentationResponse;
import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.prompt.PromptType;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class DocumentationCapability extends AbstractStructuredCapability<DocumentationRequest, DocumentationResponse> {

    public DocumentationCapability(AIEngine engine) {
        super(engine);
    }

    @Override
    public CapabilityType type() {
        return CapabilityType.DOCUMENTATION;
    }

    @Override
    protected String promptType() {
        return PromptType.DOCUMENTATION.getFileName();
    }

    @Override
    protected ToolsCategory[] tools() {
        return new ToolsCategory[]{ToolsCategory.DOCUMENTATION};
    }

    @Override
    protected Map<String, Object> variables(DocumentationRequest request) {
        return Map.of("code", request.code());
    }

    @Override
    protected Class<DocumentationResponse> responseType() {
        return DocumentationResponse.class;
    }
}
