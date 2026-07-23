package com.ai_engineering.ai_service.service;


import java.io.IOException;

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

public interface AIService {
    ChatResponse chat(ChatRequest req) throws IOException;
    ExplainResponse explain(ExplainRequest req) throws IOException;
    CodeReviewResponse codeReview(CodeReviewRequest req) throws IOException;
    SummarizeResponse summarize(SummarizeRequest req) throws IOException;
    DebugResponse debug(DebugRequest req) throws IOException;
    ArchitectureReviewResponse architecture(ArchitectureRequest req) throws IOException;
    DocumentationResponse documentation(DocumentationRequest req) throws IOException;
    RequirementAnalysisResponse requirementAnalysis(RequirementRequest req) throws IOException;
    ApiExplanationResponse apiExplanation(ApiExplanationRequest req) throws IOException;
}
