package sumit.ai.ai_engineering.agent.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sumit.ai.ai_engineering.agent.api.dto.AgentExecuteRequest;
import sumit.ai.ai_engineering.agent.api.dto.AgentExecutionDTO;
import sumit.ai.ai_engineering.agent.api.dto.AgentExecutionDetailDTO;
import sumit.ai.ai_engineering.agent.api.dto.AgentStepDTO;
import sumit.ai.ai_engineering.agent.executor.AgentExecutor;
import sumit.ai.ai_engineering.agent.model.AgentExecution;
import sumit.ai.ai_engineering.agent.model.AgentExecutionRepository;
import sumit.ai.ai_engineering.agent.model.AgentPlan;
import sumit.ai.ai_engineering.agent.model.AgentStatus;
import sumit.ai.ai_engineering.agent.model.AgentStepRepository;
import sumit.ai.ai_engineering.agent.planner.AgentPlanner;
import sumit.ai.ai_engineering.common.exception.ForbiddenAccessException;
import sumit.ai.ai_engineering.common.exception.ResourceNotFoundException;

@Service
@Transactional
public class AgentServiceImpl implements AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentServiceImpl.class);

    private final AgentPlanner agentPlanner;
    private final AgentExecutor agentExecutor;
    private final AgentExecutionRepository executionRepository;
    private final AgentStepRepository stepRepository;

    public AgentServiceImpl(
            AgentPlanner agentPlanner,
            AgentExecutor agentExecutor,
            AgentExecutionRepository executionRepository,
            AgentStepRepository stepRepository) {
        this.agentPlanner = agentPlanner;
        this.agentExecutor = agentExecutor;
        this.executionRepository = executionRepository;
        this.stepRepository = stepRepository;
    }

    @Override
    public AgentExecutionDetailDTO executeGoal(Long userId, AgentExecuteRequest request) {
        UUID executionId = UUID.randomUUID();
        log.info("Starting agent execution [id={}, userId={}, goal='{}']", executionId, userId, request.goal());

        // 1. Plan goal
        AgentPlan plan = agentPlanner.createPlan(request.goal());

        // 2. Execute plan with limits
        int maxIterations = request.maxIterations() != null ? request.maxIterations() : 10;
        boolean allowRag = request.allowRag() == null || request.allowRag();

        AgentExecution execution = agentExecutor.execute(userId, executionId, plan, maxIterations, allowRag);

        // 3. Fetch steps
        List<AgentStepDTO> steps = stepRepository.findByExecutionIdOrderByStepIndexAsc(executionId).stream()
                .map(AgentStepDTO::from)
                .toList();

        return AgentExecutionDetailDTO.from(execution, steps);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentExecutionDetailDTO getExecution(Long userId, UUID executionId) {
        AgentExecution execution = findAndValidateOwnership(userId, executionId);
        List<AgentStepDTO> steps = stepRepository.findByExecutionIdOrderByStepIndexAsc(executionId).stream()
                .map(AgentStepDTO::from)
                .toList();
        return AgentExecutionDetailDTO.from(execution, steps);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentExecutionDTO> listExecutions(Long userId) {
        return executionRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(AgentExecutionDTO::from)
                .toList();
    }

    @Override
    public AgentExecutionDTO cancelExecution(Long userId, UUID executionId) {
        AgentExecution execution = findAndValidateOwnership(userId, executionId);
        if (execution.getStatus() == AgentStatus.RUNNING) {
            execution.setStatus(AgentStatus.CANCELLED);
            execution.setCompletedAt(LocalDateTime.now());
            execution = executionRepository.save(execution);
            log.info("Cancelled agent execution [id={}, userId={}]", executionId, userId);
        }
        return AgentExecutionDTO.from(execution);
    }

    private AgentExecution findAndValidateOwnership(Long userId, UUID executionId) {
        AgentExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent execution not found: " + executionId));
        if (!execution.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You do not have access to agent execution: " + executionId);
        }
        return execution;
    }
}
