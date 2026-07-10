package com.ai_engineering.ai_service.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai_engineering.ai_service.dto.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.dto.ChatDTO.ChatResponse;
import com.ai_engineering.ai_service.dto.ExplainDTO.ExplainRequest;
import com.ai_engineering.ai_service.dto.ExplainDTO.ExplainResponse;
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
    public ResponseEntity<ChatResponse> ChatResponse(@Valid @RequestBody ChatRequest request) throws IOException{
        return ResponseEntity.ok(aiService.chat(request));
    }

    // @PostMapping("/review")
    // public ResponseEntity<CodeReviewResponse> codeReview(@RequestBody CodeReviewRequest req) {
    //     return ResponseEntity.ok(aiService.codeReview(req));
    // }
    
   @PostMapping("/explain")
   public ResponseEntity<ExplainResponse> explainResponse(@Valid @RequestBody ExplainRequest req) throws IOException {
        return ResponseEntity.ok(aiService.explain(req));
   }
   
   @PostMapping("/summarize")
   public ResponseEntity<SummarizeResponse> summarizeResponse(@Valid @RequestBody SummarizeRequest req) throws IOException{
        return ResponseEntity.ok(aiService.summarize(req));
   }
    
}
