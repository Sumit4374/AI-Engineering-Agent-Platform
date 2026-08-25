package sumit.ai.ai_engineering.ai.Capability.CodeReviewCapability;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import sumit.ai.ai_engineering.ai.Capability.IdResolver.CheckConversationId;
import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewRequest;
import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewResponse;
import sumit.ai.ai_engineering.ai.engine.AIEngine;
import sumit.ai.ai_engineering.ai.prompt.PromptType;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;



@Service
public class CodeReviewCapabilityImpl implements CodeReviewCapability{
    
    private final AIEngine engine;

    CodeReviewCapabilityImpl(AIEngine engine){
        this.engine = engine;
    }

    

    @Override
    public CodeReviewResponse execute(CodeReviewRequest request) throws IOException {
        return engine.generateStructure(
            CheckConversationId.check(request.conversationId()),
            PromptType.CODE_REVIEW.getFileName(),
            Map.of(
                "code",
                request.code()
            ), 
            CodeReviewResponse.class,
            ToolsCategory.DEVELOPMENT, ToolsCategory.DOCUMENTATION, ToolsCategory.UTILITY
        );
    }
}
