package com.ai_engineering.ai_service.engine;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import com.ai_engineering.ai_service.prompt.PromptRegistry;
import com.ai_engineering.ai_service.provider.AiProvider;
import com.ai_engineering.ai_service.provider.ProviderManager;
import com.ai_engineering.ai_service.tools.ToolRegistry;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Service
public class AIEngineImpl implements AIEngine {

    private static final Logger log = LoggerFactory.getLogger(AIEngineImpl.class);

    private final ProviderManager providerManager;
    private final PromptRegistry promptRegistry;
    private final ToolRegistry toolRegistry;

    public AIEngineImpl(ProviderManager providerManager, PromptRegistry promptRegistry, ToolRegistry toolRegistry){
        this.providerManager = providerManager;
        this.promptRegistry = promptRegistry;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public <T> T generateStructure(String conversationId, String provider, String promptType,
            Map<String, Object> variables, Class<T> responseType, ToolsCategory... tools) throws IOException {
        String prompt = promptRegistry.loadPrompt(promptType, variables);
        log.debug("Structured generation [conversationId={}, promptType={}, provider={}]",
                conversationId, promptType, provider);
        // log.debug("[promt={}]",prompt);
        return executeWithFallback(provider, promptType, client ->
            client.prompt(prompt)
                .tools(toolRegistry.getTools(tools))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .entity(responseType));
    }

    @Override
    public String generate(String conversationId, String provider, String promptType,
            Map<String, Object> variables, ToolsCategory... tools) throws IOException {
        String prompt = promptRegistry.loadPrompt(promptType, variables);
        log.debug("Text generation [conversationId={}, promptType={}, provider={}]",
                conversationId, promptType, provider);
        return executeWithFallback(provider, promptType, client ->
            client.prompt(prompt)
                .tools(toolRegistry.getTools(tools))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content());
    }

    /**
     * Run {@code call} against the resolved provider, retrying on the fallback
     * chain if it fails. The last error is rethrown when every provider fails.
     */
    private <T> T executeWithFallback(String provider, String promptType, Function<ChatClient, T> call) {
        AiProvider primary = providerManager.resolve(provider);
        List<AiProvider> chain = providerManager.fallbackChain(primary);

        RuntimeException lastError = null;
        for (AiProvider p : chain) {
            try {
                return call.apply(p.chatClient());
            } catch (RuntimeException e) {
                lastError = e;
                log.warn("Provider '{}' (model={}) failed for prompt '{}': {}. Trying next provider.",
                        p.name(), p.model(), promptType, e.getMessage());
            }
        }
        throw new IllegalStateException(
            "All AI providers failed for prompt '" + promptType + "'. Last error: "
            + (lastError == null ? "unknown" : lastError.getMessage()), lastError);
    }
}
