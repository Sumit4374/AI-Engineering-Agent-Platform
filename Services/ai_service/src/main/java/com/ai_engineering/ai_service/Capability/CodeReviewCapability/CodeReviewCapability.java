package com.ai_engineering.ai_service.Capability.CodeReviewCapability;

import java.io.IOException;

import com.ai_engineering.ai_service.dto.CodeReviewDTO.CodeReviewRequest;
import com.ai_engineering.ai_service.dto.CodeReviewDTO.CodeReviewResponse;

public interface CodeReviewCapability {
    CodeReviewResponse execute(CodeReviewRequest request) throws IOException;
}
