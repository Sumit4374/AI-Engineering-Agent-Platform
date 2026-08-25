package sumit.ai.ai_engineering.rag.chunking;

import java.util.List;
import java.util.UUID;

import sumit.ai.ai_engineering.rag.model.DocumentType;

public interface ChunkingService {

    List<Chunk> chunk(
            UUID documentId,
            Long userId,
            String text,
            String fileName,
            DocumentType documentType,
            int maxTokens,
            int overlapTokens
    );
}
