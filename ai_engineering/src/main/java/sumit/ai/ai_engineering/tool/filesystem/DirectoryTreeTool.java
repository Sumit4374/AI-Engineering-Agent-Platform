package sumit.ai.ai_engineering.tool.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.ai.tools.AiTool;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

@Component
public class DirectoryTreeTool implements AiTool {

    private static final int DEFAULT_MAX_DEPTH = 3;
    private static final int MAX_ALLOWED_DEPTH = 6;
    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            ".git", "target", "node_modules", ".idea", ".vscode", "build", ".gradle"
    );

    private final WorkspaceGuard workspaceGuard;

    public DirectoryTreeTool(WorkspaceGuard workspaceGuard) {
        this.workspaceGuard = workspaceGuard;
    }

    @Tool(description = "Generate a formatted directory tree representation of the workspace or a subdirectory.")
    public String tree(
            @ToolParam(description = "Relative directory path (use '.' or '' for workspace root)") String relativePath,
            @ToolParam(description = "Max depth to traverse (default 3, max 6)") Integer maxDepth) {
        try {
            String dir = (relativePath != null && !relativePath.isBlank()) ? relativePath : ".";
            Path root = workspaceGuard.validateAndResolve(dir);

            if (!Files.exists(root)) {
                return "Error: Directory not found: " + relativePath;
            }
            if (!Files.isDirectory(root)) {
                return "Error: Path is a file, not a directory: " + relativePath;
            }

            int depth = (maxDepth != null && maxDepth > 0) ? Math.min(maxDepth, MAX_ALLOWED_DEPTH) : DEFAULT_MAX_DEPTH;
            StringBuilder sb = new StringBuilder();
            sb.append(root.getFileName() != null ? root.getFileName().toString() : ".").append("/\n");

            buildTree(root.toFile(), 0, depth, "", sb);
            return sb.toString();

        } catch (SecurityException se) {
            return "Security Violation: " + se.getMessage();
        }
    }

    private void buildTree(File currentDir, int currentDepth, int maxDepth, String indent, StringBuilder sb) {
        if (currentDepth >= maxDepth) return;

        File[] files = currentDir.listFiles();
        if (files == null) return;

        Arrays.sort(files, Comparator.comparing(File::getName));

        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (IGNORED_DIRECTORIES.contains(f.getName())) continue;

            boolean isLast = (i == files.length - 1);
            String prefix = isLast ? "└── " : "├── ";

            sb.append(indent).append(prefix).append(f.getName());
            if (f.isDirectory()) {
                sb.append("/\n");
                buildTree(f, currentDepth + 1, maxDepth, indent + (isLast ? "    " : "│   "), sb);
            } else {
                sb.append("\n");
            }
        }
    }

    @Override
    public ToolsCategory category() {
        return ToolsCategory.FILESYSTEM;
    }
}
