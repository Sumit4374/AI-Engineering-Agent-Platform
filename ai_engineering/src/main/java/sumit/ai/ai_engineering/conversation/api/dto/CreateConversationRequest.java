package sumit.ai.ai_engineering.conversation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateConversationRequest(
    @NotBlank(message = "Conversation title must not be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title
) {}
