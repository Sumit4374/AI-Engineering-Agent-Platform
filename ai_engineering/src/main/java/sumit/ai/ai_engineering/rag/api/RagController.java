package sumit.ai.ai_engineering.rag.api;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import sumit.ai.ai_engineering.common.utility.SecurityUtils;
import sumit.ai.ai_engineering.rag.api.dto.RagQueryRequest;
import sumit.ai.ai_engineering.rag.api.dto.RagQueryResponse;
import sumit.ai.ai_engineering.rag.api.dto.RagRetrieveRequest;
import sumit.ai.ai_engineering.rag.api.dto.RetrievedChunkDTO;
import sumit.ai.ai_engineering.rag.model.RagAnswer;
import sumit.ai.ai_engineering.rag.model.RetrievedChunk;
import sumit.ai.ai_engineering.rag.retrieval.RagRetrievalService;

@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    private final RagRetrievalService ragRetrievalService;

    public RagController(RagRetrievalService ragRetrievalService) {
        this.ragRetrievalService = ragRetrievalService;
    }

    @PostMapping("/query")
    public ResponseEntity<RagQueryResponse> query(@Valid @RequestBody RagQueryRequest request) throws IOException {
        Long userId = SecurityUtils.getRequiredUserId();
        int topK = request.topK() != null ? request.topK() : 5;
        double minSimilarity = request.minSimilarity() != null ? request.minSimilarity() : 0.2;

        RagAnswer answer = ragRetrievalService.query(
                userId,
                request.conversationId(),
                request.query(),
                topK,
                minSimilarity
        );

        List<RetrievedChunkDTO> sources = answer.sources().stream()
                .map(RetrievedChunkDTO::from)
                .toList();

        return ResponseEntity.ok(new RagQueryResponse(
                answer.answer(),
                answer.conversationId(),
                sources,
                answer.totalSourcesFound()
        ));
    }

    @PostMapping("/retrieve")
    public ResponseEntity<List<RetrievedChunkDTO>> retrieve(
            @Valid @RequestBody RagRetrieveRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        int topK = request.topK() != null ? request.topK() : 5;
        double minSimilarity = request.minSimilarity() != null ? request.minSimilarity() : 0.2;

        List<RetrievedChunk> chunks = ragRetrievalService.retrieve(userId, request.query(), topK, minSimilarity);
        List<RetrievedChunkDTO> dtoList = chunks.stream()
                .map(RetrievedChunkDTO::from)
                .toList();

        return ResponseEntity.ok(dtoList);
    }
}
