package sumit.ai.ai_engineering.ai.tools.development;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sumit.ai.ai_engineering.ai.tools.development.StackTraceParserTool.ParsedStackTrace;

class StackTraceParserToolTest {

    private static final String SAMPLE_TRACE = """
            java.lang.NullPointerException: Cannot invoke method getUser() on null
            \tat com.example.app.service.UserService.processUser(UserService.java:42)
            \tat com.example.app.controller.UserController.getUser(UserController.java:28)
            \tat org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:1003)
            \tat javax.servlet.http.HttpServlet.service(HttpServlet.java:764)
            Caused by: java.io.IOException: File not found
            \tat com.example.app.repository.FileRepository.load(FileRepository.java:15)
            """;

    private StackTraceParserTool tool;

    @BeforeEach
    void setUp() {
        tool = new StackTraceParserTool();
    }

    @Test
    void parse_extractsExceptionType() {
        ParsedStackTrace result = tool.parse(SAMPLE_TRACE);
        assertThat(result.exceptionType()).isEqualTo("java.lang.NullPointerException");
    }

    @Test
    void parse_extractsMessage() {
        ParsedStackTrace result = tool.parse(SAMPLE_TRACE);
        assertThat(result.message()).isEqualTo("Cannot invoke method getUser() on null");
    }

    @Test
    void parse_extractsAllFrames() {
        ParsedStackTrace result = tool.parse(SAMPLE_TRACE);
        assertThat(result.frames()).isNotEmpty();
        // Should include at least the com.example frames + spring + javax
        assertThat(result.frames().size()).isGreaterThanOrEqualTo(4);
    }

    @Test
    void parse_extractsRootCause() {
        ParsedStackTrace result = tool.parse(SAMPLE_TRACE);
        assertThat(result.rootCause()).contains("java.io.IOException");
    }

    @Test
    void parse_filtersApplicationFrames() {
        ParsedStackTrace result = tool.parse(SAMPLE_TRACE);
        // Only com.example frames should be in applicationFrames (not spring, javax)
        List<StackTraceParserTool.StackFrame> appFrames = result.applicationFrames();
        assertThat(appFrames).isNotEmpty();
        assertThat(appFrames).allMatch(f -> f.className().startsWith("com.example"));
    }

    @Test
    void parse_nullInput_returnsEmptyResult() {
        ParsedStackTrace result = tool.parse(null);
        assertThat(result.exceptionType()).isEmpty();
        assertThat(result.frames()).isEmpty();
    }

    @Test
    void parse_blankInput_returnsEmptyResult() {
        ParsedStackTrace result = tool.parse("   ");
        assertThat(result.exceptionType()).isEmpty();
    }

    @Test
    void parse_truncatesLargeInput() {
        // Input > 32 000 chars should not throw
        String hugeInput = "a".repeat(100_000);
        assertThat(tool.parse(hugeInput)).isNotNull();
    }

    @Test
    void category_returnsDevelopment() {
        assertThat(tool.category().name()).isEqualTo("DEVELOPMENT");
    }
}
