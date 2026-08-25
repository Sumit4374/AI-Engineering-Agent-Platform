package sumit.ai.ai_engineering.tool.filesystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.ai.tools.AiTool;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

@Component
public class ReadFileTool implements AiTool {

    private static final int DEFAULT_MAX_LINES = 500;
    private static final long MAX_READ_BYTES = 256 * 1024; // 256 KB

    private final WorkspaceGuard workspaceGuard;

    public ReadFileTool(WorkspaceGuard workspaceGuard) {
        this.workspaceGuard = workspaceGuard;
    }

    @Tool(description = "Read the contents of a file within the project workspace. Enforces workspace boundaries and line limits.")
    public String readFile(
            @ToolParam(description = "Relative path to the file within the project workspace (e.g. 'pom.xml' or 'src/main/resources/application.properties')") String relativePath,
            @ToolParam(description = "Optional maximum lines to read (default 500)") Integer maxLines) {
        try {
            Path path = workspaceGuard.validateAndResolve(relativePath);

            if (!Files.exists(path)) {
                return "Error: File not found at path: " + relativePath;
            }
            if (Files.isDirectory(path)) {
                return "Error: Path is a directory, not a file: " + relativePath;
            }
            if (Files.size(path) > MAX_READ_BYTES) {
                return "Error: File size exceeds maximum allowed read limit of 256 KB: " + relativePath;
            }

            int limit = (maxLines != null && maxLines > 0) ? maxLines : DEFAULT_MAX_LINES;
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

            StringBuilder sb = new StringBuilder();
            int count = Math.min(lines.size(), limit);
            for (int i = 0; i < count; i++) {
                sb.append(lines.get(i)).append("\n");
            }
            if (lines.size() > limit) {
                sb.append(String.format("\n... [truncated: showing first %d of %d lines]", limit, lines.size()));
            }

            return sb.toString();
        } catch (SecurityException se) {
            return "Security Violation: " + se.getMessage();
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Override
    public ToolsCategory category() {
        return ToolsCategory.FILESYSTEM;
    }
}
