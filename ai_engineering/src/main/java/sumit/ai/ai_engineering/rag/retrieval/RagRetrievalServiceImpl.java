package sumit.ai.ai_engineering.rag.retrieval;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import sumit.ai.ai_engineering.ai.engine.AIEngine;
import sumit.ai.ai_engineering.ai.prompt.PromptType;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;
import sumit.ai.ai_engineering.conversation.application.ConversationService;
import sumit.ai.ai_engineering.conversation.domain.MessageRole;
import sumit.ai.ai_engineering.memory.application.ConversationMemoryService;
import sumit.ai.ai_engineering.rag.embedding.EmbeddingService;
import sumit.ai.ai_engineering.rag.model.RagAnswer;
import sumit.ai.ai_engineering.rag.model.RetrievedChunk;
import sumit.ai.ai_engineering.rag.vector.VectorStoreService;

@Service
public class RagRetrievalServiceImpl implements RagRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(RagRetrievalServiceImpl.class);

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final AIEngine aiEngine;
    private final ConversationService conversationService;
    private final ConversationMemoryService memoryService;

    public RagRetrievalServiceImpl(
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService,
            AIEngine aiEngine,
            ConversationService conversationService,
            ConversationMemoryService memoryService) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.aiEngine = aiEngine;
        this.conversationService = conversationService;
        this.memoryService = memoryService;
    }

    @Override
    public List<RetrievedChunk> retrieve(Long userId, String query, int topK, double minSimilarity) {
        if (query == null || query.isBlank() || userId == null) {
            return List.of();
        }

        int k = topK > 0 ? topK : 5;
        double threshold = minSimilarity >= 0.0 ? minSimilarity : 0.2;

        float[] queryEmbedding = embeddingService.embed(query);
        return vectorStoreService.searchSimilar(userId, queryEmbedding, k, threshold);
    }

    @Override
    public String formatContext(List<RetrievedChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "No relevant document context found.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunk c = chunks.get(i);
            sb.append(String.format("--- Context Fragment %d (Source: %s, Similarity: %.2f) ---\n",
                    i + 1, c.fileName() != null ? c.fileName() : "document", c.score()));
            sb.append(c.content().trim());
            sb.append("\n\n");
        }
        return sb.toString().trim();
    }

    @Override
    public RagAnswer query(Long userId, String conversationId, String query, int topK, double minSimilarity) throws IOException {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query must not be blank");
        }

        // 1. Retrieve relevant chunks
        List<RetrievedChunk> chunks = retrieve(userId, query, topK, minSimilarity);
        String formattedContext = formatContext(chunks);

        // 2. Resolve conversation
        String convIdStr = (conversationId != null && !conversationId.isBlank())
                ? conversationId
                : UUID.randomUUID().toString();

        if (userId != null) {
            try {
                UUID convUuid = UUID.fromString(convIdStr);
                conversationService.getOrCreateConversation(userId, convUuid, "RAG: " + query);
                conversationService.appendMessage(userId, convUuid, MessageRole.USER, query, null);
                memoryService.recordMessage(convIdStr, MessageRole.USER.name(), query);
            } catch (Exception e) {
                log.warn("Could not attach RAG query to conversation [id={}]: {}", convIdStr, e.getMessage());
            }
        }

        // 3. Generate augmented response
        String answer = aiEngine.generate(
                convIdStr,
                PromptType.RAG.getFileName(),
                Map.of(
                        "context", formattedContext,
                        "question", query
                ),
                ToolsCategory.DOCUMENTATION, ToolsCategory.UTILITY
        );

        // 4. Record assistant answer in conversation & memory
        if (userId != null) {
            try {
                UUID convUuid = UUID.fromString(convIdStr);
                conversationService.appendMessage(userId, convUuid, MessageRole.ASSISTANT, answer, null);
                memoryService.recordMessage(convIdStr, MessageRole.ASSISTANT.name(), answer);
            } catch (Exception e) {
                log.warn("Could not save RAG response to conversation [id={}]: {}", convIdStr, e.getMessage());
            }
        }

        log.info("RAG query completed [userId={}, sourcesFound={}, conversationId={}]",
                userId, chunks.size(), convIdStr);

        return new RagAnswer(answer, convIdStr, chunks, chunks.size());
    }
}
