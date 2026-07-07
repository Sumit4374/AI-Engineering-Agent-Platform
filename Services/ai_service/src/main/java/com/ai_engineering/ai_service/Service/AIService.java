package com.ai_engineering.ai_service.Service;


import java.io.IOException;

import com.ai_engineering.ai_service.DTOs.ChatDTO.ChatRequest;
import com.ai_engineering.ai_service.DTOs.ChatDTO.ChatResponse;
// import com.ai_engineering.ai_service.DTOs.ReviewDTO.CodeReviewRequest;
// import com.ai_engineering.ai_service.DTOs.ReviewDTO.CodeReviewResponse;

public interface AIService {
    ChatResponse chat(ChatRequest req) throws IOException;
    // CodeReviewResponse codeReview(CodeReviewRequest req);
}
