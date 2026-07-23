package com.ai_engineering.ai_service.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import com.ai_engineering.ai_service.service.AIService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/ai")
public class AIController {
    private final AIService aiService;

    public AIController(AIService aiService){
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) throws IOException{
        return ResponseEntity.ok(aiService.chat(request));
    }

    @PostMapping("/review")
    public ResponseEntity<CodeReviewResponse> codeReview(@Valid @RequestBody CodeReviewRequest req) throws IOException {
        return ResponseEntity.ok(aiService.codeReview(req));
    }

    @PostMapping("/explain")
    public ResponseEntity<ExplainResponse> explain(@Valid @RequestBody ExplainRequest req) throws IOException {
        return ResponseEntity.ok(aiService.explain(req));
    }

    @PostMapping("/summarize")
    public ResponseEntity<SummarizeResponse> summarize(@Valid @RequestBody SummarizeRequest req) throws IOException{
        return ResponseEntity.ok(aiService.summarize(req));
    }

    @PostMapping("/debug")
    public ResponseEntity<DebugResponse> debug(@Valid @RequestBody DebugRequest req) throws IOException {
        return ResponseEntity.ok(aiService.debug(req));
    }

    @PostMapping("/architecture")
    public ResponseEntity<ArchitectureReviewResponse> architecture(@Valid @RequestBody ArchitectureRequest req) throws IOException {
        return ResponseEntity.ok(aiService.architecture(req));
    }

    @PostMapping("/documentation")
    public ResponseEntity<DocumentationResponse> documentation(@Valid @RequestBody DocumentationRequest req) throws IOException {
        return ResponseEntity.ok(aiService.documentation(req));
    }

    @PostMapping("/requirements")
    public ResponseEntity<RequirementAnalysisResponse> requirementAnalysis(@Valid @RequestBody RequirementRequest req) throws IOException {
        return ResponseEntity.ok(aiService.requirementAnalysis(req));
    }

    @PostMapping("/api-explanation")
    public ResponseEntity<ApiExplanationResponse> apiExplanation(@Valid @RequestBody ApiExplanationRequest req) throws IOException {
        return ResponseEntity.ok(aiService.apiExplanation(req));
    }

}
