package sumit.ai.ai_engineering.ai.tools.development;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.ai.tools.AiTool;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

/**
 * Deterministic stack trace parser.
 *
 * <p>This tool extracts structured facts from a Java (or JVM-compatible) stack trace.
 * It does NOT diagnose or reason about the root cause — that is the LLM's responsibility.
 * The tool only extracts information that is objectively present in the text.
 *
 * <p>Security: input is treated as plain text and never executed.
 * Input length is capped to prevent resource exhaustion.
 */
@Component
public class StackTraceParserTool implements AiTool {

    private static final int MAX_INPUT_LENGTH = 32_000;

    // Matches: java.lang.NullPointerException: some message
    private static final Pattern EXCEPTION_LINE =
            Pattern.compile("^([\\w$.]+(?:Exception|Error|Throwable|Fault)[\\w$.]*)(?::\\s*(.*))?$",
                    Pattern.MULTILINE);

    // Matches: at com.example.MyClass.myMethod(MyClass.java:42)
    private static final Pattern STACK_FRAME =
            Pattern.compile("^\\s+at\\s+([\\w$.]+)\\.([\\w$<>]+)\\(([^)]+)\\)",
                    Pattern.MULTILINE);

    // Matches: Caused by: java.io.IOException: ...
    private static final Pattern CAUSED_BY =
            Pattern.compile("^Caused by:\\s+(.+)$", Pattern.MULTILINE);

    /**
     * Parses a JVM stack trace string and extracts structured information.
     * The LLM should use this output to reason about the failure; the tool only extracts facts.
     *
     * @param stackTrace the raw stack trace text (max 32 000 characters)
     * @return a {@link ParsedStackTrace} record with structured data
     */
    @Tool(description = """
            Parse a JVM stack trace and extract structured information: exception type, message,
            stack frames, root cause (Caused by), and application-level frames.
            This tool extracts facts only — it does not diagnose or explain the error.
            The LLM performs the reasoning based on the extracted data.
            """)
    public ParsedStackTrace parse(
            @ToolParam(description = "The raw JVM stack trace text to parse (max 32 000 chars)") String stackTrace) {

        if (stackTrace == null || stackTrace.isBlank()) {
            return new ParsedStackTrace("", "", List.of(), "", List.of());
        }
        if (stackTrace.length() > MAX_INPUT_LENGTH) {
            stackTrace = stackTrace.substring(0, MAX_INPUT_LENGTH);
        }

        // Extract primary exception
        String exceptionType = "";
        String message = "";
        Matcher exMatcher = EXCEPTION_LINE.matcher(stackTrace);
        if (exMatcher.find()) {
            exceptionType = exMatcher.group(1);
            message = exMatcher.group(2) != null ? exMatcher.group(2).trim() : "";
        }

        // Extract all stack frames
        List<StackFrame> frames = new ArrayList<>();
        Matcher frameMatcher = STACK_FRAME.matcher(stackTrace);
        while (frameMatcher.find()) {
            frames.add(new StackFrame(
                    frameMatcher.group(1),
                    frameMatcher.group(2),
                    frameMatcher.group(3)
            ));
        }

        // Extract root cause (last "Caused by")
        String rootCause = "";
        Matcher causedByMatcher = CAUSED_BY.matcher(stackTrace);
        while (causedByMatcher.find()) {
            rootCause = causedByMatcher.group(1).trim();
        }

        // Filter application-level frames (non-JDK, non-library)
        List<StackFrame> appFrames = frames.stream()
                .filter(f -> !f.className().startsWith("java.")
                        && !f.className().startsWith("javax.")
                        && !f.className().startsWith("jakarta.")
                        && !f.className().startsWith("sun.")
                        && !f.className().startsWith("com.sun.")
                        && !f.className().startsWith("org.springframework.")
                        && !f.className().startsWith("org.apache.")
                        && !f.className().startsWith("com.fasterxml.")
                        && !f.className().startsWith("io.netty.")
                        && !f.className().startsWith("reactor."))
                .toList();

        return new ParsedStackTrace(exceptionType, message, frames, rootCause, appFrames);
    }

    // ---------------------------------------------------------------
    // Result types
    // ---------------------------------------------------------------

    public record StackFrame(
            String className,
            String methodName,
            String location
    ) {}

    public record ParsedStackTrace(
            String exceptionType,
            String message,
            List<StackFrame> frames,
            String rootCause,
            List<StackFrame> applicationFrames
    ) {}

    @Override
    public ToolsCategory category() {
        return ToolsCategory.DEVELOPMENT;
    }
}
