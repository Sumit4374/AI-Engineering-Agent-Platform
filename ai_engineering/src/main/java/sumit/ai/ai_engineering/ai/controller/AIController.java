package sumit.ai.ai_engineering.ai.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatRequest;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatResponse;
import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewRequest;
import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewResponse;
import sumit.ai.ai_engineering.ai.dto.ExplainDTO.ExplainRequest;
import sumit.ai.ai_engineering.ai.dto.ExplainDTO.ExplainResponse;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeRequest;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeResponse;
import sumit.ai.ai_engineering.ai.service.AIService;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * REST controller for all AI capabilities and model providers.
 *
 * <p>Controllers must remain thin: validate input, delegate to the service layer,
 * map the result to an HTTP response. No AI infrastructure concerns belong here.
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * Blocking chat — returns a single response when the model finishes.
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) throws IOException {
        return ResponseEntity.ok(aiService.chat(request));
    }

    /**
     * Streaming chat — emits tokens as Server-Sent Events as they arrive from the model.
     * The client receives a {@code text/event-stream} response.
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@Valid @RequestBody ChatRequest request) throws IOException {
        return aiService.streamChat(request);
    }

    /**
     * Code review — returns a structured {@link CodeReviewResponse} as JSON.
     */
    @PostMapping("/review")
    public ResponseEntity<CodeReviewResponse> codeReview(@Valid @RequestBody CodeReviewRequest req) throws IOException {
        return ResponseEntity.ok(aiService.codeReview(req));
    }

    /**
     * Explain — returns a free-text explanation of the requested topic.
     */
    @PostMapping("/explain")
    public ResponseEntity<ExplainResponse> explain(@Valid @RequestBody ExplainRequest req) throws IOException {
        return ResponseEntity.ok(aiService.explain(req));
    }

    /**
     * Summarize — returns a structured summary of the provided text.
     */
    @PostMapping("/summarize")
    public ResponseEntity<SummarizeResponse> summarize(@Valid @RequestBody SummarizeRequest req) throws IOException {
        return ResponseEntity.ok(aiService.summarize(req));
    }
}
