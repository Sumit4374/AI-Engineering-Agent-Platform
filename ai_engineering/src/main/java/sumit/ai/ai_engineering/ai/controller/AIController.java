package sumit.ai.ai_engineering.ai.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import jakarta.validation.Valid;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatRequest;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatResponse;
import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewRequest;
import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewResponse;
import sumit.ai.ai_engineering.ai.dto.ExplainDTO.ExplainRequest;
import sumit.ai.ai_engineering.ai.dto.ExplainDTO.ExplainResponse;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeRequest;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeResponse;
import sumit.ai.ai_engineering.ai.service.AIService;


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
   public ResponseEntity<ExplainResponse> explainResponse(@Valid @RequestBody ExplainRequest req) throws IOException {
        return ResponseEntity.ok(aiService.explain(req));
   }
   
   @PostMapping("/summarize")
   public ResponseEntity<SummarizeResponse> summarizeResponse(@Valid @RequestBody SummarizeRequest req) throws IOException{
        return ResponseEntity.ok(aiService.summarize(req));
   }
    
}
