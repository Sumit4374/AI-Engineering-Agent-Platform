package sumit.ai.ai_engineering.agent.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentStepRepository extends JpaRepository<AgentStep, UUID> {

    List<AgentStep> findByExecutionIdOrderByStepIndexAsc(UUID executionId);

    void deleteByExecutionId(UUID executionId);
}
