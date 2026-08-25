package sumit.ai.ai_engineering.agent.application;

import java.util.List;
import java.util.UUID;

import sumit.ai.ai_engineering.agent.api.dto.AgentExecuteRequest;
import sumit.ai.ai_engineering.agent.api.dto.AgentExecutionDTO;
import sumit.ai.ai_engineering.agent.api.dto.AgentExecutionDetailDTO;

public interface AgentService {

    AgentExecutionDetailDTO executeGoal(Long userId, AgentExecuteRequest request);

    AgentExecutionDetailDTO getExecution(Long userId, UUID executionId);

    List<AgentExecutionDTO> listExecutions(Long userId);

    AgentExecutionDTO cancelExecution(Long userId, UUID executionId);
}
