package sumit.ai.ai_engineering.ai.engine;

import java.io.IOException;
import java.util.Map;


import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

public interface AIEngine {

    String generate(
        String conversationId,
        String promptType,
        Map<String, Object> variables,
        ToolsCategory... tools
    ) throws IOException;

    <T> T generateStructure(
        String conversationId,
        String promptType,
        Map<String, Object> variables,
        Class<T> responseType,
        ToolsCategory... tools
    ) throws IOException;

    Flux<String> stream(
        String conversationID,
        String promptType,
        Map<String,Object> variables,
        ToolsCategory... tools
    ) throws IOException;

}
