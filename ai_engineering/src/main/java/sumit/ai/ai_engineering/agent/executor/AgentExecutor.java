package sumit.ai.ai_engineering.agent.executor;

import java.util.UUID;

import sumit.ai.ai_engineering.agent.model.AgentExecution;
import sumit.ai.ai_engineering.agent.model.AgentPlan;

public interface AgentExecutor {

    AgentExecution execute(Long userId, UUID executionId, AgentPlan plan, int maxIterations, boolean allowRag);
}
