package sumit.ai.ai_engineering.agent.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgentExecuteRequest(
    @NotBlank(message = "Goal must not be blank")
    @Size(max = 2000, message = "Goal must not exceed 2000 characters")
    String goal,

    Integer maxIterations,

    Boolean allowRag
) {}
