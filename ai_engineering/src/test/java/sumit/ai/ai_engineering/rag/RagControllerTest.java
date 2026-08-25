package sumit.ai.ai_engineering.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import sumit.ai.ai_engineering.rag.api.RagController;
import sumit.ai.ai_engineering.rag.api.dto.RagQueryRequest;
import sumit.ai.ai_engineering.rag.api.dto.RagQueryResponse;
import sumit.ai.ai_engineering.rag.api.dto.RagRetrieveRequest;
import sumit.ai.ai_engineering.rag.api.dto.RetrievedChunkDTO;
import sumit.ai.ai_engineering.rag.model.RagAnswer;
import sumit.ai.ai_engineering.rag.model.RetrievedChunk;
import sumit.ai.ai_engineering.rag.retrieval.RagRetrievalService;
import sumit.ai.ai_engineering.user.Configuration.CustomUserDetails.CustomUserDetails;
import sumit.ai.ai_engineering.user.Model.User;
import sumit.ai.ai_engineering.user.Model.Enum.Role;

@ExtendWith(MockitoExtension.class)
class RagControllerTest {

    @Mock private RagRetrievalService ragRetrievalService;

    private RagController controller;

    private final Long userId = 2L;

    @BeforeEach
    void setUp() {
        controller = new RagController(ragRetrievalService);
        User user = User.builder().id(userId).userName("raguser").role(Role.USER).build();
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
    void query_validRequest_returnsRagQueryResponse() throws IOException {
        RagQueryRequest req = new RagQueryRequest("conv-1", "Explain modular monolith", 5, 0.3);
        RetrievedChunk chunk = new RetrievedChunk(
                UUID.randomUUID(), UUID.randomUUID(), "Modular monolith organizes code into modules", 0.9, "arch.md", "arch.md", 1, "markdown"
        );
        RagAnswer answer = new RagAnswer("A modular monolith has single deployment unit...", "conv-1", List.of(chunk), 1);

        when(ragRetrievalService.query(eq(userId), eq("conv-1"), eq("Explain modular monolith"), eq(5), eq(0.3)))
                .thenReturn(answer);

        ResponseEntity<RagQueryResponse> response = controller.query(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().answer()).contains("modular monolith");
        assertThat(response.getBody().sources()).hasSize(1);
    }

    @Test
    void retrieve_validRequest_returnsListOfRetrievedChunks() {
        RagRetrieveRequest req = new RagRetrieveRequest("JWT token", 3, 0.4);
        RetrievedChunk chunk = new RetrievedChunk(
                UUID.randomUUID(), UUID.randomUUID(), "JWT token contains claims", 0.85, "security.md", "security.md", null, "markdown"
        );

        when(ragRetrievalService.retrieve(eq(userId), eq("JWT token"), eq(3), eq(0.4)))
                .thenReturn(List.of(chunk));

        ResponseEntity<List<RetrievedChunkDTO>> response = controller.retrieve(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).content()).contains("JWT token contains claims");
    }
}
