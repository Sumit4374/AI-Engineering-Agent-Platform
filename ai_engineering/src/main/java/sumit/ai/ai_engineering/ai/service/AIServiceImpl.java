package sumit.ai.ai_engineering.ai.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.Capability.ChatCapability.ChatCapability;
import sumit.ai.ai_engineering.ai.Capability.CodeReviewCapability.CodeReviewCapability;
import sumit.ai.ai_engineering.ai.Capability.SummaryCapability.SummaryCapability;
import sumit.ai.ai_engineering.ai.Capability.IdResolver.CheckConversationId;
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
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

/**
 * Application service that orchestrates AI capabilities.
 *
 * <p>All AI operations are delegated to the appropriate {@link sumit.ai.ai_engineering.ai.Capability}
 * implementation, which in turn delegates to the {@link AIEngine}. This service must remain
 * free of AI infrastructure concerns (prompt loading, ChatClient, tool binding).
 */
@Service
public class AIServiceImpl implements AIService {

    private final AIEngine engine;
    private final ChatCapability chatCapability;
    private final CodeReviewCapability codeReviewCapability;
    private final SummaryCapability summaryCapability;

    public AIServiceImpl(
            AIEngine engine,
            ChatCapability chatCapability,
            CodeReviewCapability codeReviewCapability,
            SummaryCapability summaryCapability) {
        this.engine = engine;
        this.chatCapability = chatCapability;
        this.codeReviewCapability = codeReviewCapability;
        this.summaryCapability = summaryCapability;
    }

    @Override
    public ChatResponse chat(ChatRequest req) throws IOException {
        return chatCapability.execute(req);
    }

    @Override
    public Flux<String> streamChat(ChatRequest req) throws IOException {
        return chatCapability.stream(req);
    }

    @Override
    public ExplainResponse explain(ExplainRequest req) throws IOException {
        return new ExplainResponse(
            engine.generate(
                CheckConversationId.check(req.conversationId()),
                PromptType.EXPLAIN.getFileName(),
                Map.of("topic", req.topic()),
                ToolsCategory.DOCUMENTATION
            )
        );
    }

    @Override
    public SummarizeResponse summarize(SummarizeRequest req) throws IOException {
        return summaryCapability.execute(req);
    }

    @Override
    public CodeReviewResponse codeReview(CodeReviewRequest req) throws IOException {
        return codeReviewCapability.execute(req);
    }
}
