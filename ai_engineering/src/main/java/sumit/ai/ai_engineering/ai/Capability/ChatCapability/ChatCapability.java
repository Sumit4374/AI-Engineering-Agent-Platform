package sumit.ai.ai_engineering.ai.Capability.ChatCapability;

import java.io.IOException;


import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatRequest;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatResponse;

public interface ChatCapability {
    ChatResponse execute(ChatRequest request) throws IOException;
    Flux<String> stream(ChatRequest request) throws IOException;
}
