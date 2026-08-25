package sumit.ai.ai_engineering.ai.engine;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;


import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.prompt.PromptRegistry;
import sumit.ai.ai_engineering.ai.tools.ToolRegistry;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

@Service
public class AIEngineImpl implements AIEngine {

    private static final Logger log = LoggerFactory.getLogger(AIEngineImpl.class);

    private final ChatClient chatClient;
    private final PromptRegistry promptRegistry;
    private final ToolRegistry toolRegistry;

    public AIEngineImpl(ChatClient chatClient, PromptRegistry promptRegistry, ToolRegistry toolRegistry){
        this.chatClient = chatClient;
        this.promptRegistry = promptRegistry;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public <T> T generateStructure(String conversationId, String promptType, Map<String, Object> variables,
            Class<T> responseType, ToolsCategory... tools) throws IOException {
        String prompt = promptRegistry.loadPrompt(promptType, variables);
        log.debug("Structured generation [conversationId={}, promptType={}]", conversationId, promptType);
        return chatClient
            .prompt(prompt)
            .tools(toolRegistry.getTools(tools))
            .advisors(
                a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)
            )
            .call()
            .entity(responseType);
    }

    @Override
    public String generate(String conversationId, String promptType, Map<String, Object> variables,
            ToolsCategory... tools) throws IOException {
        String prompt = promptRegistry.loadPrompt(promptType, variables);
        log.debug("Text generation [conversationId={}, promptType={}]", conversationId, promptType);
        return chatClient
            .prompt(prompt)
            .tools(toolRegistry.getTools(tools))
            .advisors(
                a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)
            )
            .call()
            .content();
    }

    @Override
    public Flux<String> stream(
                                String conversationId, 
                                String promptType,
                                Map<String, Object> variables,
                                ToolsCategory... tools
    ) throws IOException {
        String prompt = promptRegistry.loadPrompt(promptType, variables);
        log.debug("Text generation [conversationId={}, promptType={}]", conversationId, promptType);
        return chatClient
            .prompt(prompt)
            .tools(toolRegistry.getTools(tools))
            .advisors(
                a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)
            ).stream()
            .content();
    }
}
