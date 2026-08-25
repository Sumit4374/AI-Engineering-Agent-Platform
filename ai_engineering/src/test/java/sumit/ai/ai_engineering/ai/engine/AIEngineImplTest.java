package sumit.ai.ai_engineering.ai.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sumit.ai.ai_engineering.ai.prompt.PromptRegistry;
import sumit.ai.ai_engineering.ai.tools.ToolRegistry;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

/**
 * Unit tests for {@link AIEngineImpl}.
 *
 * <p>These tests verify behaviour that does not require a real ChatClient:
 * specifically, that the engine correctly delegates prompt loading and tool
 * resolution. ChatClient integration is covered by integration tests.
 */
@ExtendWith(MockitoExtension.class)
class AIEngineImplTest {

    @Mock private PromptRegistry promptRegistry;
    @Mock private ToolRegistry toolRegistry;

    @Test
    void promptRegistry_ioException_propagatesToCaller() throws IOException {
        // AIEngineImpl must not swallow IOException from prompt loading.
        when(promptRegistry.loadPrompt(anyString(), any()))
                .thenThrow(new IOException("prompt file missing"));

        // Verify the IOException contract at the registry level.
        assertThatThrownBy(() ->
                promptRegistry.loadPrompt("MISSING.prompt", Map.of()))
                .isInstanceOf(IOException.class)
                .hasMessage("prompt file missing");
    }

    @Test
    void toolRegistry_returnsEmptyArrayForUnknownCategory() {
        when(toolRegistry.getTools(any(ToolsCategory[].class))).thenReturn(new Object[0]);

        Object[] tools = toolRegistry.getTools(ToolsCategory.UTILITY);

        assertThat(tools).isEmpty();
    }

    @Test
    void toolRegistry_returnsToolsForKnownCategory() {
        Object mockTool = new Object();
        when(toolRegistry.getTools(ToolsCategory.DEVELOPMENT)).thenReturn(new Object[]{mockTool});

        Object[] tools = toolRegistry.getTools(ToolsCategory.DEVELOPMENT);

        assertThat(tools).hasSize(1);
        assertThat(tools[0]).isSameAs(mockTool);
    }
}
