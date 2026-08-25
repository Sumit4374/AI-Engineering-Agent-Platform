package sumit.ai.ai_engineering.ai.service;

import java.io.IOException;

import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatRequest;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatResponse;
import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewRequest;
import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewResponse;
import sumit.ai.ai_engineering.ai.dto.ExplainDTO.ExplainRequest;
import sumit.ai.ai_engineering.ai.dto.ExplainDTO.ExplainResponse;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeRequest;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeResponse;

public interface AIService {
    ChatResponse chat(ChatRequest req) throws IOException;
    Flux<String> streamChat(ChatRequest req) throws IOException;
    ExplainResponse explain(ExplainRequest req) throws IOException;
    CodeReviewResponse codeReview(CodeReviewRequest req) throws IOException;
    SummarizeResponse summarize(SummarizeRequest req) throws IOException;
}
