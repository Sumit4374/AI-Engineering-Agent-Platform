package sumit.ai.ai_engineering.tool.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.ai.tools.AiTool;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

@Component
public class SearchInProjectTool implements AiTool {

    private static final int DEFAULT_MAX_RESULTS = 20;
    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            ".git", "target", "node_modules", ".idea", ".vscode", "build", ".gradle"
    );

    private final WorkspaceGuard workspaceGuard;

    public SearchInProjectTool(WorkspaceGuard workspaceGuard) {
        this.workspaceGuard = workspaceGuard;
    }

    @Tool(description = "Search for a keyword or regex pattern in project source files within the workspace.")
    public List<SearchResult> search(
            @ToolParam(description = "Search query string or regex pattern") String query,
            @ToolParam(description = "Optional file extension filter, e.g. '.java' or '.xml'") String extensionFilter,
            @ToolParam(description = "Max results to return (default 20)") Integer maxResults) {

        if (query == null || query.isBlank()) {
            return List.of();
        }

        int limit = (maxResults != null && maxResults > 0) ? maxResults : DEFAULT_MAX_RESULTS;
        Path root = workspaceGuard.getWorkspaceRoot();
        List<SearchResult> results = new ArrayList<>();
        Pattern pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);

        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                  .filter(p -> !isIgnored(p, root))
                  .filter(p -> extensionFilter == null || extensionFilter.isBlank() || p.toString().endsWith(extensionFilter))
                  .forEach(p -> {
                      if (results.size() >= limit) return;
                      searchFile(p, root, pattern, results, limit);
                  });
        } catch (IOException ignored) {
        }

        return results;
    }

    private void searchFile(Path filePath, Path root, Pattern pattern, List<SearchResult> results, int limit) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 0; i < lines.size(); i++) {
                if (results.size() >= limit) return;
                String line = lines.get(i);
                if (pattern.matcher(line).find()) {
                    String relative = root.relativize(filePath).toString();
                    results.add(new SearchResult(relative, i + 1, line.trim()));
                }
            }
        } catch (Exception ignored) {
            // Binary or unreadable file
        }
    }

    private boolean isIgnored(Path path, Path root) {
        Path relative = root.relativize(path);
        for (Path component : relative) {
            if (IGNORED_DIRECTORIES.contains(component.toString())) {
                return true;
            }
        }
        return false;
    }

    public record SearchResult(
            String file,
            int lineNumber,
            String matchingLine
    ) {}

    @Override
    public ToolsCategory category() {
        return ToolsCategory.FILESYSTEM;
    }
}
