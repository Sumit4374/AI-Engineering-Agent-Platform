package sumit.ai.ai_engineering.conversation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import sumit.ai.ai_engineering.conversation.domain.MessageRole;

public record AppendMessageRequest(
    @NotNull(message = "Role is required")
    MessageRole role,

    @NotBlank(message = "Content must not be blank")
    String content,

    Integer tokenUsage
) {}
