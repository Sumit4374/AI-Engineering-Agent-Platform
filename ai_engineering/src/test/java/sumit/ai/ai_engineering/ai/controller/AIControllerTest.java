package sumit.ai.ai_engineering.ai.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatRequest;
import sumit.ai.ai_engineering.ai.dto.ChatDTO.ChatResponse;
import sumit.ai.ai_engineering.ai.dto.CodeReviewDTO.CodeReviewRequest;
import sumit.ai.ai_engineering.ai.dto.ExplainDTO.ExplainRequest;
import sumit.ai.ai_engineering.ai.dto.ExplainDTO.ExplainResponse;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeRequest;
import sumit.ai.ai_engineering.ai.provider.ModelProviderRegistry;
import sumit.ai.ai_engineering.ai.provider.ModelProviderRegistry.ModelProviderInfo;
import sumit.ai.ai_engineering.ai.service.AIService;

/**
 * Pure unit tests for {@link AIController} — no Spring context required.
 */
@ExtendWith(MockitoExtension.class)
class AIControllerTest {

    @Mock private AIService aiService;
    @Mock private ModelProviderRegistry modelProviderRegistry;

    private AIController controller;

    @BeforeEach
    void setUp() {
        controller = new AIController(aiService, modelProviderRegistry);
    }

    // ---- /chat ----

    @Test
    void chat_delegatesToServiceAndReturns200() throws IOException {
        when(aiService.chat(any(ChatRequest.class)))
                .thenReturn(new ChatResponse("Hello from AI"));

        ResponseEntity<ChatResponse> response = controller.chat(
                new ChatRequest("conv-1", "What is Java?"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().response()).isEqualTo("Hello from AI");
    }

    @Test
    void chat_serviceThrowsIOException_propagatesException() throws IOException {
        when(aiService.chat(any(ChatRequest.class))).thenThrow(new IOException("LLM unavailable"));

        org.junit.jupiter.api.Assertions.assertThrows(IOException.class,
                () -> controller.chat(new ChatRequest("conv-1", "question")));
    }

    // ---- /explain ----

    @Test
    void explain_delegatesToServiceAndReturns200() throws IOException {
        when(aiService.explain(any(ExplainRequest.class)))
                .thenReturn(new ExplainResponse("Dependency Injection is a design pattern..."));

        ResponseEntity<ExplainResponse> response = controller.explain(
                new ExplainRequest("conv-1", "Dependency Injection"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().explanation()).contains("Dependency Injection");
    }

    // ---- /chat/stream ----

    @Test
    void streamChat_returnsFluxFromService() throws IOException {
        Flux<String> expectedFlux = Flux.just("Hello", " world");
        when(aiService.streamChat(any(ChatRequest.class))).thenReturn(expectedFlux);

        Flux<String> result = controller.streamChat(new ChatRequest("conv-1", "Hello"));

        assertThat(result.collectList().block()).containsExactly("Hello", " world");
    }

    // ---- /providers ----

    @Test
    void getProviders_returnsListFromRegistry() {
        when(modelProviderRegistry.getAllProviders()).thenReturn(List.of(
                new ModelProviderInfo("NVIDIA_NIM", "NVIDIA NIM", "meta/llama-3.1-70b-instruct", true, true)
        ));

        ResponseEntity<List<ModelProviderInfo>> resp = controller.getProviders();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
        assertThat(resp.getBody().get(0).type()).isEqualTo("NVIDIA_NIM");
    }
}
