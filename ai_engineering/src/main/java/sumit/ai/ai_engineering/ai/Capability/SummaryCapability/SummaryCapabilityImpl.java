package sumit.ai.ai_engineering.ai.Capability.SummaryCapability;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;



import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeRequest;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeResponse;
import sumit.ai.ai_engineering.ai.engine.AIEngine;
import sumit.ai.ai_engineering.ai.prompt.PromptType;
import sumit.ai.ai_engineering.ai.prompt.SummaryTypeLoader;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

@Service
public class SummaryCapabilityImpl implements SummaryCapability{

    private final AIEngine engine;
    private final SummaryTypeLoader summaryTypeLoader;

    private String getConversationId(String conversationId){
        if(conversationId==null || conversationId.isBlank()){
            return UUID.randomUUID().toString();
        }
        return conversationId;
    }

    SummaryCapabilityImpl(AIEngine engine, SummaryTypeLoader summaryTypeLoader){
        this.engine = engine;
        this.summaryTypeLoader = summaryTypeLoader;
    }

    @Override
    public SummarizeResponse execute(SummarizeRequest request) throws IOException {
        return engine.generateStructure(
            getConversationId(request.conversationId()),
            summaryTypeLoader.loadSummaryType(request.type()),
            Map.of(
                "text",request.text()
            ),
            SummarizeResponse.class
        );
    }
    @Override
    public Flux<String> stream(SummarizeRequest request) throws IOException {
        return engine.stream(
            getConversationId(request.conversationId()),
            PromptType.SUMMARIZATION.getFileName(),
            Map.of(
                "content",
                request.text()
            ), 
            ToolsCategory.UTILITY
        );
    }
}
