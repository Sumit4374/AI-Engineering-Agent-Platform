package sumit.ai.ai_engineering.ai.Capability.ChatCapability;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatRequest;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatResponse;
import sumit.ai.ai_engineering.ai.engine.AIEngine;
import sumit.ai.ai_engineering.ai.prompt.PromptType;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;
import sumit.ai.ai_engineering.common.utility.SecurityUtils;
import sumit.ai.ai_engineering.conversation.application.ConversationService;
import sumit.ai.ai_engineering.conversation.domain.Conversation;
import sumit.ai.ai_engineering.conversation.domain.MessageRole;
import sumit.ai.ai_engineering.memory.application.ConversationMemoryService;

@Service
public class ChatCapabilityImpl implements ChatCapability {

    private static final Logger log = LoggerFactory.getLogger(ChatCapabilityImpl.class);

    private final AIEngine engine;
    private final ConversationService conversationService;
    private final ConversationMemoryService memoryService;

    public ChatCapabilityImpl(AIEngine engine,
                              ConversationService conversationService,
                              ConversationMemoryService memoryService) {
        this.engine = engine;
        this.conversationService = conversationService;
        this.memoryService = memoryService;
    }

    @Override
    public ChatResponse execute(ChatRequest request) throws IOException {
        String convIdStr = resolveConversationId(request.conversationId(), request.request());
        
        // Record user message in memory
        memoryService.recordMessage(convIdStr, MessageRole.USER.name(), request.request());
        persistMessageIfAuthenticated(convIdStr, MessageRole.USER, request.request());

        // Generate response
        String answer = engine.generate(
                convIdStr,
                PromptType.CHAT.getFileName(),
                Map.of("question", request.request()),
                ToolsCategory.UTILITY
        );

        // Record assistant response
        memoryService.recordMessage(convIdStr, MessageRole.ASSISTANT.name(), answer);
        persistMessageIfAuthenticated(convIdStr, MessageRole.ASSISTANT, answer);

        return new ChatResponse(answer, convIdStr);
    }

    @Override
    public Flux<String> stream(ChatRequest request) throws IOException {
        String convIdStr = resolveConversationId(request.conversationId(), request.request());

        memoryService.recordMessage(convIdStr, MessageRole.USER.name(), request.request());
        persistMessageIfAuthenticated(convIdStr, MessageRole.USER, request.request());

        StringBuilder fullResponse = new StringBuilder();

        return engine.stream(
                convIdStr,
                PromptType.CHAT.getFileName(),
                Map.of("question", request.request()),
                ToolsCategory.UTILITY
        ).doOnNext(fullResponse::append)
         .doOnComplete(() -> {
             String answer = fullResponse.toString();
             if (!answer.isBlank()) {
                 memoryService.recordMessage(convIdStr, MessageRole.ASSISTANT.name(), answer);
                 persistMessageIfAuthenticated(convIdStr, MessageRole.ASSISTANT, answer);
             }
         });
    }

    private String resolveConversationId(String requestedId, String initialText) {
        Optional<Long> currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.isPresent()) {
            UUID convUuid = null;
            if (requestedId != null && !requestedId.isBlank()) {
                try {
                    convUuid = UUID.fromString(requestedId);
                } catch (IllegalArgumentException ignored) {
                    convUuid = UUID.randomUUID();
                }
            }
            String title = (initialText != null && initialText.length() > 40)
                    ? initialText.substring(0, 40) + "..."
                    : initialText;
            Conversation conv = conversationService.getOrCreateConversation(currentUserId.get(), convUuid, title);
            return conv.getId().toString();
        }

        if (requestedId != null && !requestedId.isBlank()) {
            return requestedId;
        }
        return UUID.randomUUID().toString();
    }

    private void persistMessageIfAuthenticated(String convIdStr, MessageRole role, String content) {
        Optional<Long> currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.isPresent()) {
            try {
                UUID convUuid = UUID.fromString(convIdStr);
                conversationService.appendMessage(currentUserId.get(), convUuid, role, content, null);
            } catch (Exception e) {
                log.warn("Could not persist message for conversation [id={}] to database: {}", convIdStr, e.getMessage());
            }
        }
    }
}
