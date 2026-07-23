package com.ai_engineering.ai_service.capability;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

/**
 * Base class for capabilities that return free-form text from the LLM.
 *
 * <p>Subclasses declare the prompt, the tool categories to expose, how the
 * request maps to prompt variables, and how to wrap the raw text into the
 * response DTO.
 */
public abstract class AbstractTextCapability<REQ extends CapabilityRequest, RES>
        implements Capability<REQ, RES> {

    protected final AIEngine engine;

    protected AbstractTextCapability(AIEngine engine) {
        this.engine = engine;
    }

    /** Prompt file name (e.g. {@code PromptType.CHAT.getFileName()}). */
    protected abstract String promptType();

    /** Tool categories exposed to the LLM for this capability. */
    protected abstract ToolsCategory[] tools();

    /** Map the request into the prompt template variables. */
    protected abstract Map<String, Object> variables(REQ request);

    /** Wrap the generated text into the capability's response DTO. */
    protected abstract RES map(String content);

    @Override
    public RES execute(REQ request) throws IOException {
        String content = engine.generate(
                resolveConversationId(request.conversationId()),
                promptType(),
                variables(request),
                tools());
        return map(content);
    }

    protected String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return conversationId;
    }
}
