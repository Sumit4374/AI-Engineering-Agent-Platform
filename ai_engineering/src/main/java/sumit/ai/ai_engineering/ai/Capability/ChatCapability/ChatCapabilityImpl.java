package sumit.ai.ai_engineering.ai.Capability.ChatCapability;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatRequest;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatResponse;
import sumit.ai.ai_engineering.ai.engine.AIEngine;
import sumit.ai.ai_engineering.ai.prompt.PromptType;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

@Service
public class ChatCapabilityImpl implements ChatCapability {

    private final AIEngine engine;

    ChatCapabilityImpl(AIEngine engine){
        this.engine = engine;
    }

    @Override
    public ChatResponse execute(ChatRequest request) throws IOException {
        return new ChatResponse(
            engine.generate(request.conversationId(),
            PromptType.CHAT.getFileName(),
            Map.of("question",request.request()),
            ToolsCategory.UTILITY)
        );
    }

    @Override
    public Flux<String> stream(ChatRequest request) throws IOException{
        return engine.stream(
            request.conversationId(),
            PromptType.CHAT.getFileName(),
            Map.of(
                "question",
                request.request()
            ), 
            ToolsCategory.UTILITY
        );
    }
    
}
