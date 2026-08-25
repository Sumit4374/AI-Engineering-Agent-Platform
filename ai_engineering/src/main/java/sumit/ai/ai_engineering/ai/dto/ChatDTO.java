package sumit.ai.ai_engineering.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatDTO {

    
    public record ChatRequest(
        String conversationId,
        @NotBlank(message = "Request should not be empty")
        String request
    ){}
    public record ChatResponse(String response){}
}
