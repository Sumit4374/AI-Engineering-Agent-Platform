package sumit.ai.ai_engineering.ai.Capability.CodeReviewCapability;

import java.io.IOException;

import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewRequest;
import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewResponse;


public interface CodeReviewCapability {
    CodeReviewResponse execute(CodeReviewRequest request) throws IOException;
}
