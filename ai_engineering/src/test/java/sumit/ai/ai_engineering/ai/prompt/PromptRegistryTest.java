package sumit.ai.ai_engineering.ai.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromptRegistryTest {

    @Mock
    private PromptLoader promptLoader;

    private PromptRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new PromptRegistry(promptLoader);
    }

    @Test
    void loadPrompt_rendersVariables_returnsInterpolatedString() throws IOException {
        when(promptLoader.load("CHAT.prompt")).thenReturn("Hello {name}, your question: {question}");

        String result = registry.loadPrompt("CHAT.prompt", Map.of("name", "Alice", "question", "What is Java?"));

        assertThat(result).isEqualTo("Hello Alice, your question: What is Java?");
    }

    @Test
    void loadPrompt_noVariables_returnsTemplateUnchanged() throws IOException {
        when(promptLoader.load("EXPLAIN.prompt")).thenReturn("Explain the concept clearly.");

        String result = registry.loadPrompt("EXPLAIN.prompt", Map.of());

        assertThat(result).isEqualTo("Explain the concept clearly.");
    }

    @Test
    void loadPrompt_loaderThrowsIoException_propagatesException() throws IOException {
        when(promptLoader.load("MISSING.prompt")).thenThrow(new IOException("File not found"));

        assertThatThrownBy(() -> registry.loadPrompt("MISSING.prompt", Map.of()))
                .isInstanceOf(IOException.class)
                .hasMessage("File not found");
    }
}
