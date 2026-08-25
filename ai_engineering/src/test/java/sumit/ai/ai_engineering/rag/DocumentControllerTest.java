package sumit.ai.ai_engineering.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import sumit.ai.ai_engineering.rag.api.DocumentController;
import sumit.ai.ai_engineering.rag.api.dto.DocumentDTO;
import sumit.ai.ai_engineering.rag.api.dto.DocumentUploadResponse;
import sumit.ai.ai_engineering.rag.api.dto.IngestTextRequest;
import sumit.ai.ai_engineering.rag.ingestion.DocumentIngestionService;
import sumit.ai.ai_engineering.rag.model.Document;
import sumit.ai.ai_engineering.rag.model.DocumentRepository;
import sumit.ai.ai_engineering.rag.model.DocumentStatus;
import sumit.ai.ai_engineering.rag.model.DocumentType;
import sumit.ai.ai_engineering.user.Configuration.CustomUserDetails.CustomUserDetails;
import sumit.ai.ai_engineering.user.Model.User;
import sumit.ai.ai_engineering.user.Model.Enum.Role;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock private DocumentIngestionService ingestionService;
    @Mock private DocumentRepository documentRepository;

    private DocumentController controller;

    private final Long userId = 5L;
    private final UUID docId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new DocumentController(ingestionService, documentRepository);
        User user = User.builder().id(userId).userName("docuser").role(Role.USER).build();
        CustomUserDetails details = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadDocument_validFile_returnsCreated() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "architecture.md", "text/markdown", "# Architecture Overview".getBytes()
        );

        Document doc = Document.builder()
                .id(docId)
                .userId(userId)
                .fileName("architecture.md")
                .status(DocumentStatus.READY)
                .totalChunks(3)
                .build();

        when(ingestionService.ingestDocument(eq(userId), eq("architecture.md"), eq("text/markdown"), any()))
                .thenReturn(doc);

        ResponseEntity<DocumentUploadResponse> response = controller.uploadDocument(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalChunks()).isEqualTo(3);
    }

    @Test
    void ingestRawText_validRequest_returnsCreated() {
        IngestTextRequest req = new IngestTextRequest("notes", "Meeting notes content");
        Document doc = Document.builder()
                .id(docId)
                .userId(userId)
                .fileName("notes.txt")
                .status(DocumentStatus.READY)
                .totalChunks(1)
                .build();

        when(ingestionService.ingestRawText(userId, "notes", "Meeting notes content")).thenReturn(doc);

        ResponseEntity<DocumentUploadResponse> response = controller.ingestRawText(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().fileName()).isEqualTo("notes.txt");
    }

    @Test
    void listDocuments_returnsUserDocuments() {
        Document doc = Document.builder()
                .id(docId)
                .userId(userId)
                .fileName("test.txt")
                .contentType("text/plain")
                .documentType(DocumentType.TEXT)
                .fileSize(100L)
                .status(DocumentStatus.READY)
                .totalChunks(1)
                .createdAt(LocalDateTime.now())
                .build();

        when(documentRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(doc));

        ResponseEntity<List<DocumentDTO>> response = controller.listDocuments();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getDocument_ownedByUser_returnsDocument() {
        Document doc = Document.builder()
                .id(docId)
                .userId(userId)
                .fileName("test.txt")
                .contentType("text/plain")
                .documentType(DocumentType.TEXT)
                .fileSize(100L)
                .status(DocumentStatus.READY)
                .totalChunks(1)
                .createdAt(LocalDateTime.now())
                .build();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        ResponseEntity<DocumentDTO> response = controller.getDocument(docId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(docId);
    }

    @Test
    void deleteDocument_delegatesToService() {
        ResponseEntity<Void> response = controller.deleteDocument(docId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(ingestionService).deleteDocument(userId, docId);
    }
}
