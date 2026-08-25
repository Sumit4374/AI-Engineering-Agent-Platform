package sumit.ai.ai_engineering.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sumit.ai.ai_engineering.agent.api.dto.AgentExecuteRequest;
import sumit.ai.ai_engineering.agent.api.dto.AgentExecutionDTO;
import sumit.ai.ai_engineering.agent.api.dto.AgentExecutionDetailDTO;
import sumit.ai.ai_engineering.agent.application.AgentServiceImpl;
import sumit.ai.ai_engineering.agent.executor.AgentExecutor;
import sumit.ai.ai_engineering.agent.model.AgentExecution;
import sumit.ai.ai_engineering.agent.model.AgentExecutionRepository;
import sumit.ai.ai_engineering.agent.model.AgentPlan;
import sumit.ai.ai_engineering.agent.model.AgentStatus;
import sumit.ai.ai_engineering.agent.model.AgentStepRepository;
import sumit.ai.ai_engineering.agent.planner.AgentPlanner;
import sumit.ai.ai_engineering.common.exception.ForbiddenAccessException;
import sumit.ai.ai_engineering.common.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock private AgentPlanner agentPlanner;
    @Mock private AgentExecutor agentExecutor;
    @Mock private AgentExecutionRepository executionRepository;
    @Mock private AgentStepRepository stepRepository;

    private AgentServiceImpl agentService;

    private final Long userId = 1L;
    private final UUID executionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        agentService = new AgentServiceImpl(agentPlanner, agentExecutor, executionRepository, stepRepository);
    }

    @Test
    void executeGoal_plansAndExecutesGoal() {
        AgentExecuteRequest req = new AgentExecuteRequest("Analyze project", 5, true);
        AgentPlan plan = new AgentPlan("Analyze project", "Strategy", List.of());
        AgentExecution execution = AgentExecution.builder()
                .id(executionId)
                .userId(userId)
                .goal("Analyze project")
                .status(AgentStatus.COMPLETED)
                .result("Done")
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        when(agentPlanner.createPlan("Analyze project")).thenReturn(plan);
        when(agentExecutor.execute(eq(userId), any(UUID.class), eq(plan), eq(5), eq(true))).thenReturn(execution);
        when(stepRepository.findByExecutionIdOrderByStepIndexAsc(any())).thenReturn(List.of());

        AgentExecutionDetailDTO result = agentService.executeGoal(userId, req);

        assertThat(result.goal()).isEqualTo("Analyze project");
        assertThat(result.status()).isEqualTo(AgentStatus.COMPLETED);
        verify(agentPlanner).createPlan("Analyze project");
    }

    @Test
    void getExecution_ownedByUser_returnsDetail() {
        AgentExecution execution = AgentExecution.builder()
                .id(executionId)
                .userId(userId)
                .goal("Goal")
                .status(AgentStatus.COMPLETED)
                .startedAt(LocalDateTime.now())
                .build();

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(stepRepository.findByExecutionIdOrderByStepIndexAsc(executionId)).thenReturn(List.of());

        AgentExecutionDetailDTO result = agentService.getExecution(userId, executionId);

        assertThat(result.id()).isEqualTo(executionId);
    }

    @Test
    void getExecution_notOwnedByUser_throwsForbiddenAccessException() {
        AgentExecution execution = AgentExecution.builder()
                .id(executionId)
                .userId(999L)
                .build();

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        assertThatThrownBy(() -> agentService.getExecution(userId, executionId))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void cancelExecution_runningExecution_setsCancelled() {
        AgentExecution execution = AgentExecution.builder()
                .id(executionId)
                .userId(userId)
                .goal("Goal")
                .status(AgentStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AgentExecutionDTO result = agentService.cancelExecution(userId, executionId);

        assertThat(result.status()).isEqualTo(AgentStatus.CANCELLED);
    }
}
