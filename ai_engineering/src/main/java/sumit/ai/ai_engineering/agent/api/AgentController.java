package sumit.ai.ai_engineering.agent.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import sumit.ai.ai_engineering.agent.api.dto.AgentExecuteRequest;
import sumit.ai.ai_engineering.agent.api.dto.AgentExecutionDTO;
import sumit.ai.ai_engineering.agent.api.dto.AgentExecutionDetailDTO;
import sumit.ai.ai_engineering.agent.application.AgentService;
import sumit.ai.ai_engineering.common.utility.SecurityUtils;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/execute")
    public ResponseEntity<AgentExecutionDetailDTO> executeGoal(
            @Valid @RequestBody AgentExecuteRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        AgentExecutionDetailDTO result = agentService.executeGoal(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentExecutionDetailDTO> getExecution(@PathVariable("id") UUID id) {
        Long userId = SecurityUtils.getRequiredUserId();
        return ResponseEntity.ok(agentService.getExecution(userId, id));
    }

    @GetMapping
    public ResponseEntity<List<AgentExecutionDTO>> listExecutions() {
        Long userId = SecurityUtils.getRequiredUserId();
        return ResponseEntity.ok(agentService.listExecutions(userId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<AgentExecutionDTO> cancelExecution(@PathVariable("id") UUID id) {
        Long userId = SecurityUtils.getRequiredUserId();
        return ResponseEntity.ok(agentService.cancelExecution(userId, id));
    }
}
