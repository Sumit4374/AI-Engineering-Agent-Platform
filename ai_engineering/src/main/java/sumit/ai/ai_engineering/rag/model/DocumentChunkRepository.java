package sumit.ai.ai_engineering.rag.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(UUID documentId);

    List<DocumentChunk> findByUserId(Long userId);

    void deleteByDocumentId(UUID documentId);

    long countByDocumentId(UUID documentId);
}
