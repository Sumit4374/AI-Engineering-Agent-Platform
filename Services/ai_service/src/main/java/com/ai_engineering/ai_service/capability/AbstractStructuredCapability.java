package com.ai_engineering.ai_service.capability;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.ai_engineering.ai_service.engine.AIEngine;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

/**
 * Base class for capabilities that return a structured (typed) response mapped
 * from the LLM output via Spring AI's {@code .entity()} binding.
 *
 * <p>Here the response type IS the object returned to the caller, so there is
 * no separate mapping step.
 */
public abstract class AbstractStructuredCapability<REQ extends CapabilityRequest, RES>
        implements Capability<REQ, RES> {

    protected final AIEngine engine;

    protected AbstractStructuredCapability(AIEngine engine) {
        this.engine = engine;
    }

    /** Prompt file name (e.g. {@code PromptType.CODE_REVIEW.getFileName()}). */
    protected abstract String promptType();

    /** Tool categories exposed to the LLM for this capability. */
    protected abstract ToolsCategory[] tools();

    /** Map the request into the prompt template variables. */
    protected abstract Map<String, Object> variables(REQ request);

    /** The structured type the LLM output is bound to. */
    protected abstract Class<RES> responseType();

    @Override
    public RES execute(REQ request) throws IOException {
        return engine.generateStructure(
                resolveConversationId(request.conversationId()),
                request.provider(),
                promptType(),
                variables(request),
                responseType(),
                tools());
    }

    protected String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return conversationId;
    }
}
