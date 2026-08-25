package sumit.ai.ai_engineering.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import sumit.ai.ai_engineering.agent.api.AgentController;
import sumit.ai.ai_engineering.agent.api.dto.AgentExecuteRequest;
import sumit.ai.ai_engineering.agent.api.dto.AgentExecutionDTO;
import sumit.ai.ai_engineering.agent.api.dto.AgentExecutionDetailDTO;
import sumit.ai.ai_engineering.agent.application.AgentService;
import sumit.ai.ai_engineering.agent.model.AgentStatus;
import sumit.ai.ai_engineering.user.Configuration.CustomUserDetails.CustomUserDetails;
import sumit.ai.ai_engineering.user.Model.User;
import sumit.ai.ai_engineering.user.Model.Enum.Role;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    @Mock private AgentService agentService;

    private AgentController controller;

    private final Long userId = 1L;
    private final UUID executionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new AgentController(agentService);
        User user = User.builder().id(userId).userName("testuser").role(Role.USER).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void executeGoal_returnsCreated() {
        AgentExecuteRequest req = new AgentExecuteRequest("Goal", 5, true);
        AgentExecutionDetailDTO detail = new AgentExecutionDetailDTO(
                executionId, userId, "Goal", AgentStatus.COMPLETED, 1, 100, "{}", "Done", null, LocalDateTime.now(), LocalDateTime.now(), List.of()
        );

        when(agentService.executeGoal(eq(userId), eq(req))).thenReturn(detail);

        ResponseEntity<AgentExecutionDetailDTO> resp = controller.executeGoal(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().goal()).isEqualTo("Goal");
    }

    @Test
    void getExecution_returnsOk() {
        AgentExecutionDetailDTO detail = new AgentExecutionDetailDTO(
                executionId, userId, "Goal", AgentStatus.COMPLETED, 1, 100, "{}", "Done", null, LocalDateTime.now(), LocalDateTime.now(), List.of()
        );

        when(agentService.getExecution(userId, executionId)).thenReturn(detail);

        ResponseEntity<AgentExecutionDetailDTO> resp = controller.getExecution(executionId);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().id()).isEqualTo(executionId);
    }

    @Test
    void listExecutions_returnsList() {
        AgentExecutionDTO dto = new AgentExecutionDTO(
                executionId, userId, "Goal", AgentStatus.COMPLETED, 1, 100, LocalDateTime.now(), LocalDateTime.now()
        );

        when(agentService.listExecutions(userId)).thenReturn(List.of(dto));

        ResponseEntity<List<AgentExecutionDTO>> resp = controller.listExecutions();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    void cancelExecution_returnsUpdatedStatus() {
        AgentExecutionDTO dto = new AgentExecutionDTO(
                executionId, userId, "Goal", AgentStatus.CANCELLED, 1, 100, LocalDateTime.now(), LocalDateTime.now()
        );

        when(agentService.cancelExecution(userId, executionId)).thenReturn(dto);

        ResponseEntity<AgentExecutionDTO> resp = controller.cancelExecution(executionId);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().status()).isEqualTo(AgentStatus.CANCELLED);
    }
}
