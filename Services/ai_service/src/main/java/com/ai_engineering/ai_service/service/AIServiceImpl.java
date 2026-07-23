package com.ai_engineering.ai_service.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.ai_engineering.ai_service.capability.CapabilityRegistry;
import com.ai_engineering.ai_service.capability.CapabilityType;
import com.ai_engineering.ai_service.dto.ApiExplanationDTO.ApiExplanationRequest;
import com.ai_engineering.ai_service.dto.ApiExplanationDTO.ApiExplanationResponse;
import com.ai_engineering.ai_service.dto.ArchitectureDTO.ArchitectureRequest;
import com.ai_engineering.ai_service.dto.ArchitectureDTO.ArchitectureReviewResponse;
import com.ai_engineering.ai_service.dto.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.dto.ChatDTO.ChatResponse;
import com.ai_engineering.ai_service.dto.CodeReviewDTO.CodeReviewRequest;
import com.ai_engineering.ai_service.dto.CodeReviewDTO.CodeReviewResponse;
import com.ai_engineering.ai_service.dto.DebugDTO.DebugRequest;
import com.ai_engineering.ai_service.dto.DebugDTO.DebugResponse;
import com.ai_engineering.ai_service.dto.DocumentationDTO.DocumentationRequest;
import com.ai_engineering.ai_service.dto.DocumentationDTO.DocumentationResponse;
import com.ai_engineering.ai_service.dto.ExplainDTO.ExplainRequest;
import com.ai_engineering.ai_service.dto.ExplainDTO.ExplainResponse;
import com.ai_engineering.ai_service.dto.RequirementDTO.RequirementAnalysisResponse;
import com.ai_engineering.ai_service.dto.RequirementDTO.RequirementRequest;
import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeRequest;
import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeResponse;

/**
 * Thin facade over the {@link CapabilityRegistry}. Each method maps a typed
 * request to its {@link CapabilityType}; all execution logic lives in the
 * capability beans.
 */
@Service
public class AIServiceImpl implements AIService {

    private final CapabilityRegistry capabilities;

    AIServiceImpl(CapabilityRegistry capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public ChatResponse chat(ChatRequest req) throws IOException {
        return capabilities.execute(CapabilityType.CHAT, req);
    }

    @Override
    public ExplainResponse explain(ExplainRequest req) throws IOException {
        return capabilities.execute(CapabilityType.EXPLAIN, req);
    }

    @Override
    public CodeReviewResponse codeReview(CodeReviewRequest req) throws IOException {
        return capabilities.execute(CapabilityType.CODE_REVIEW, req);
    }

    @Override
    public SummarizeResponse summarize(SummarizeRequest req) throws IOException {
        return capabilities.execute(CapabilityType.SUMMARIZATION, req);
    }

    @Override
    public DebugResponse debug(DebugRequest req) throws IOException {
        return capabilities.execute(CapabilityType.DEBUG, req);
    }

    @Override
    public ArchitectureReviewResponse architecture(ArchitectureRequest req) throws IOException {
        return capabilities.execute(CapabilityType.ARCHITECTURE, req);
    }

    @Override
    public DocumentationResponse documentation(DocumentationRequest req) throws IOException {
        return capabilities.execute(CapabilityType.DOCUMENTATION, req);
    }

    @Override
    public RequirementAnalysisResponse requirementAnalysis(RequirementRequest req) throws IOException {
        return capabilities.execute(CapabilityType.REQUIREMENT_ANALYSIS, req);
    }

    @Override
    public ApiExplanationResponse apiExplanation(ApiExplanationRequest req) throws IOException {
        return capabilities.execute(CapabilityType.API_EXPLANATION, req);
    }
}
