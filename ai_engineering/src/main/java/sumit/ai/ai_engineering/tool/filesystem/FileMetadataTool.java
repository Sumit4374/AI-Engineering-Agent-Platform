package sumit.ai.ai_engineering.tool.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.ai.tools.AiTool;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

@Component
public class FileMetadataTool implements AiTool {

    private final WorkspaceGuard workspaceGuard;

    public FileMetadataTool(WorkspaceGuard workspaceGuard) {
        this.workspaceGuard = workspaceGuard;
    }

    @Tool(description = "Get metadata facts about a file or directory within the workspace: size, type, last modified, line count.")
    public FileMetadata getMetadata(
            @ToolParam(description = "Relative path to file or directory within workspace") String relativePath) {
        try {
            Path path = workspaceGuard.validateAndResolve(relativePath);

            if (!Files.exists(path)) {
                return new FileMetadata(relativePath, false, false, 0, 0, "", "NOT_FOUND");
            }

            boolean isDir = Files.isDirectory(path);
            long size = isDir ? 0 : Files.size(path);
            FileTime modified = Files.getLastModifiedTime(path);
            int lines = 0;
            if (!isDir && size < 512 * 1024) {
                try {
                    lines = Files.readAllLines(path).size();
                } catch (Exception ignored) {
                }
            }

            String filename = path.getFileName() != null ? path.getFileName().toString() : "";
            int dotIdx = filename.lastIndexOf('.');
            String ext = (dotIdx > 0 && dotIdx < filename.length() - 1) ? filename.substring(dotIdx) : "";

            return new FileMetadata(
                    relativePath,
                    true,
                    isDir,
                    size,
                    lines,
                    ext,
                    modified.toString()
            );

        } catch (SecurityException se) {
            return new FileMetadata(relativePath, false, false, 0, 0, "", "SECURITY_ERROR: " + se.getMessage());
        } catch (IOException e) {
            return new FileMetadata(relativePath, false, false, 0, 0, "", "IO_ERROR: " + e.getMessage());
        }
    }

    public record FileMetadata(
            String path,
            boolean exists,
            boolean isDirectory,
            long sizeBytes,
            int lineCount,
            String extension,
            String lastModified
    ) {}

    @Override
    public ToolsCategory category() {
        return ToolsCategory.FILESYSTEM;
    }
}
