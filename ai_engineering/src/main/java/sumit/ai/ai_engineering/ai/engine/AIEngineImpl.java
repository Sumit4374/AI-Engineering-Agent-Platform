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
import sumit.ai.ai_engineering.ai.provider.ModelProvider;
import sumit.ai.ai_engineering.ai.provider.ModelProviderRegistry;
import sumit.ai.ai_engineering.ai.tools.ToolRegistry;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

@Service
public class AIEngineImpl implements AIEngine {

    private static final Logger log = LoggerFactory.getLogger(AIEngineImpl.class);

    private final ModelProviderRegistry modelProviderRegistry;
    private final PromptRegistry promptRegistry;
    private final ToolRegistry toolRegistry;
    private final ChatClient fallbackChatClient;

    public AIEngineImpl(
            ModelProviderRegistry modelProviderRegistry,
            PromptRegistry promptRegistry,
            ToolRegistry toolRegistry,
            ChatClient chatClient) {
        this.modelProviderRegistry = modelProviderRegistry;
        this.promptRegistry = promptRegistry;
        this.toolRegistry = toolRegistry;
        this.fallbackChatClient = chatClient;
    }

    private ChatClient getChatClient() {
        if (modelProviderRegistry != null) {
            ModelProvider active = modelProviderRegistry.getActiveProvider();
            if (active != null && active.getChatClient() != null) {
                return active.getChatClient();
            }
        }
        return fallbackChatClient;
    }

    @Override
    public <T> T generateStructure(String conversationId, String promptType, Map<String, Object> variables,
            Class<T> responseType, ToolsCategory... tools) throws IOException {
        String prompt = promptRegistry.loadPrompt(promptType, variables);
        log.debug("Structured generation [conversationId={}, promptType={}]", conversationId, promptType);
        return getChatClient()
                .prompt(prompt)
                .tools(toolRegistry.getTools(tools))
                .advisors(
                        a -> {
                            if (conversationId != null) {
                                a.param(ChatMemory.CONVERSATION_ID, conversationId);
                            }
                        }
                )
                .call()
                .entity(responseType);
    }

    @Override
    public String generate(String conversationId, String promptType, Map<String, Object> variables,
            ToolsCategory... tools) throws IOException {
        String prompt = promptRegistry.loadPrompt(promptType, variables);
        log.debug("Text generation [conversationId={}, promptType={}]", conversationId, promptType);
        return getChatClient()
                .prompt(prompt)
                .tools(toolRegistry.getTools(tools))
                .advisors(
                        a -> {
                            if (conversationId != null) {
                                a.param(ChatMemory.CONVERSATION_ID, conversationId);
                            }
                        }
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
        log.debug("SSE streaming started [conversationId={}, promptType={}]", conversationId, promptType);

        return getChatClient()
                .prompt(prompt)
                .tools(toolRegistry.getTools(tools))
                .advisors(
                        a -> {
                            if (conversationId != null) {
                                a.param(ChatMemory.CONVERSATION_ID, conversationId);
                            }
                        }
                )
                .stream()
                .content()
                .doOnCancel(() -> log.debug("SSE client disconnected / cancelled stream [conversationId={}]", conversationId))
                .doOnError(err -> log.error("SSE stream error [conversationId={}]: {}", conversationId, err.getMessage()))
                .onErrorResume(err -> Flux.just("\n\n[Streaming error: " + err.getMessage() + "]"));
    }
}
