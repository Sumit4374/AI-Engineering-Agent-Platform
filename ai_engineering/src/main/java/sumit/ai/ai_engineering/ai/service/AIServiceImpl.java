package sumit.ai.ai_engineering.ai.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import sumit.ai.ai_engineering.ai.Capability.ChatCapability.ChatCapability;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatRequest;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatResponse;
import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewRequest;
import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewResponse;
import sumit.ai.ai_engineering.ai.dto.ExplainDTO.ExplainRequest;
import sumit.ai.ai_engineering.ai.dto.ExplainDTO.ExplainResponse;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeRequest;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeResponse;
import sumit.ai.ai_engineering.ai.engine.AIEngine;
import sumit.ai.ai_engineering.ai.prompt.PromptType;
import sumit.ai.ai_engineering.ai.prompt.SummaryTypeLoader;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;



@Service
public class AIServiceImpl implements AIService {

    private final AIEngine engine;
    private final SummaryTypeLoader summaryTypeLoader;
    private final ChatCapability chatCapability;

    AIServiceImpl(AIEngine engine, SummaryTypeLoader summaryTypeLoader, ChatCapability chatCapability) {
        this.engine = engine;
        this.summaryTypeLoader = summaryTypeLoader;
        this.chatCapability = chatCapability;
    }

    private String getConversationId(String conversationId){
        if(conversationId==null || conversationId.isBlank()){
            return UUID.randomUUID().toString();
        }
        return conversationId;
    }

    @Override
    public ChatResponse chat(ChatRequest req) throws IOException{
        return chatCapability.execute(req);
    }

    @Override
    public ExplainResponse explain(ExplainRequest req) throws IOException {
        return new ExplainResponse(
            engine.generate(
                getConversationId(req.conversationId()),
                PromptType.EXPLAIN.getFileName(),
                Map.of(
                    "topic",req.topic()
                ),
                ToolsCategory.DOCUMENTATION
            )
        );
    }

    @Override
    public SummarizeResponse summarize(SummarizeRequest req) throws IOException {
        return engine.generateStructure(
                getConversationId(req.conversationId()),
                summaryTypeLoader.loadSummaryType(req.type()),
                Map.of(
                    "text",req.text()
                ),
                SummarizeResponse.class);
    }

    @Override
    public CodeReviewResponse codeReview(CodeReviewRequest req) throws IOException {
        return engine.generateStructure(
            getConversationId(req.conversationId()),
            PromptType.CODE_REVIEW.getFileName(),
            Map.of(
                "code",req.code()
            ),
            CodeReviewResponse.class,
            ToolsCategory.DEVELOPMENT);
    }

}
