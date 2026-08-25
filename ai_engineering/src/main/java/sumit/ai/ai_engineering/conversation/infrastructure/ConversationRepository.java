package sumit.ai.ai_engineering.conversation.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sumit.ai.ai_engineering.conversation.domain.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<Conversation> findByIdAndUserId(UUID id, Long userId);

    boolean existsByIdAndUserId(UUID id, Long userId);

    void deleteByIdAndUserId(UUID id, Long userId);
}
