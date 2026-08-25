package sumit.ai.ai_engineering.rag.api;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import sumit.ai.ai_engineering.common.exception.ForbiddenAccessException;
import sumit.ai.ai_engineering.common.exception.ResourceNotFoundException;
import sumit.ai.ai_engineering.common.utility.SecurityUtils;
import sumit.ai.ai_engineering.rag.api.dto.DocumentDTO;
import sumit.ai.ai_engineering.rag.api.dto.DocumentUploadResponse;
import sumit.ai.ai_engineering.rag.api.dto.IngestTextRequest;
import sumit.ai.ai_engineering.rag.ingestion.DocumentIngestionService;
import sumit.ai.ai_engineering.rag.model.Document;
import sumit.ai.ai_engineering.rag.model.DocumentRepository;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentIngestionService ingestionService;
    private final DocumentRepository documentRepository;

    public DocumentController(DocumentIngestionService ingestionService,
                              DocumentRepository documentRepository) {
        this.ingestionService = ingestionService;
        this.documentRepository = documentRepository;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file) throws IOException {
        Long userId = SecurityUtils.getRequiredUserId();
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        Document doc = ingestionService.ingestDocument(
                userId,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new DocumentUploadResponse(
                doc.getId(),
                doc.getFileName(),
                doc.getStatus(),
                doc.getTotalChunks(),
                "Document uploaded and processed into " + doc.getTotalChunks() + " vector chunks"
        ));
    }

    @PostMapping("/text")
    public ResponseEntity<DocumentUploadResponse> ingestRawText(
            @Valid @RequestBody IngestTextRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        Document doc = ingestionService.ingestRawText(userId, request.title(), request.content());

        return ResponseEntity.status(HttpStatus.CREATED).body(new DocumentUploadResponse(
                doc.getId(),
                doc.getFileName(),
                doc.getStatus(),
                doc.getTotalChunks(),
                "Text content processed into " + doc.getTotalChunks() + " vector chunks"
        ));
    }

    @GetMapping
    public ResponseEntity<List<DocumentDTO>> listDocuments() {
        Long userId = SecurityUtils.getRequiredUserId();
        List<DocumentDTO> list = documentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(DocumentDTO::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocument(@PathVariable("id") UUID id) {
        Long userId = SecurityUtils.getRequiredUserId();
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        if (!doc.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You do not have access to document: " + id);
        }

        return ResponseEntity.ok(DocumentDTO.from(doc));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable("id") UUID id) {
        Long userId = SecurityUtils.getRequiredUserId();
        ingestionService.deleteDocument(userId, id);
        return ResponseEntity.noContent().build();
    }
}
