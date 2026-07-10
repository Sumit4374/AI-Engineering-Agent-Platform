package com.ai_engineering.ai_service.service;


import java.io.IOException;

import com.ai_engineering.ai_service.dto.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.dto.ChatDTO.ChatResponse;
import com.ai_engineering.ai_service.dto.ExplainDTO.ExplainRequest;
import com.ai_engineering.ai_service.dto.ExplainDTO.ExplainResponse;
import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeRequest;
import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeResponse;

public interface AIService {
    ChatResponse chat(ChatRequest req) throws IOException;
    ExplainResponse explain(ExplainRequest req) throws IOException;
    // CodeReviewResponse codeReview(CodeReviewRequest req);
    SummarizeResponse summarize(SummarizeRequest req) throws IOException;
}
