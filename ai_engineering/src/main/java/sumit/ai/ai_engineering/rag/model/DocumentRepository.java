package sumit.ai.ai_engineering.rag.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Document> findByIdAndUserId(UUID id, Long userId);

    boolean existsByIdAndUserId(UUID id, Long userId);

    void deleteByIdAndUserId(UUID id, Long userId);
}
