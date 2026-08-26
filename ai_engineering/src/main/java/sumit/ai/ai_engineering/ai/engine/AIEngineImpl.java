package sumit.ai.ai_engineering.ai.engine;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.prompt.PromptRegistry;
import sumit.ai.ai_engineering.ai.tools.ToolRegistry;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;
import sumit.ai.ai_engineering.infrastructure.observability.PlatformMetrics;
import sumit.ai.ai_engineering.memory.model.MemoryMessage;

@Service
public class AIEngineImpl implements AIEngine {

    private static final Logger log = LoggerFactory.getLogger(AIEngineImpl.class);

    private final PromptRegistry promptRegistry;
    private final ToolRegistry toolRegistry;
    private final ChatClient chatClient;
    private final ObjectProvider<PlatformMetrics> platformMetricsProvider;

    public AIEngineImpl(
            PromptRegistry promptRegistry,
            ToolRegistry toolRegistry,
            ChatClient chatClient,
            ObjectProvider<PlatformMetrics> platformMetricsProvider) {
        this.promptRegistry = promptRegistry;
        this.toolRegistry = toolRegistry;
        this.chatClient = chatClient;
        this.platformMetricsProvider = platformMetricsProvider;
    }

    private String getActiveProviderName() {
        return "OPENAI";
    }

    @Override
    public <T> T generateStructure(String conversationId, String promptType, Map<String, Object> variables,
            Class<T> responseType, ToolsCategory... tools) throws IOException {
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            String prompt = promptRegistry.loadPrompt(promptType, variables);
            log.debug("Structured generation [conversationId={}, promptType={}]", conversationId, promptType);
            T result = chatClient
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
            success = true;
            return result;
        } finally {
            long duration = System.currentTimeMillis() - start;
            PlatformMetrics metrics = platformMetricsProvider.getIfAvailable();
            if (metrics != null) {
                metrics.recordLlmCall(getActiveProviderName(), promptType, duration, success, 0);
            }
        }
    }

    @Override
    public String generate(String conversationId, String promptType, Map<String, Object> variables,
            ToolsCategory... tools) throws IOException {
        long start = System.currentTimeMillis();
        boolean success = false;
        int tokens = 0;
        try {
            String prompt = promptRegistry.loadPrompt(promptType, variables);
            log.debug("Text generation [conversationId={}, promptType={}]", conversationId, promptType);
            String result = chatClient
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
            tokens = MemoryMessage.estimateTokens(result);
            success = true;
            return result;
        } finally {
            long duration = System.currentTimeMillis() - start;
            PlatformMetrics metrics = platformMetricsProvider.getIfAvailable();
            if (metrics != null) {
                metrics.recordLlmCall(getActiveProviderName(), promptType, duration, success, tokens);
            }
        }
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

        return chatClient
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
