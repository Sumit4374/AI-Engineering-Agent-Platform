package sumit.ai.ai_engineering.agent.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentExecutionRepository extends JpaRepository<AgentExecution, UUID> {

    List<AgentExecution> findByUserIdOrderByStartedAtDesc(Long userId);

    Optional<AgentExecution> findByIdAndUserId(UUID id, Long userId);

    boolean existsByIdAndUserId(UUID id, Long userId);
}
