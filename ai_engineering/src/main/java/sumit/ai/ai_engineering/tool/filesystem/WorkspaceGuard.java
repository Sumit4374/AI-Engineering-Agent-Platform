package sumit.ai.ai_engineering.tool.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Security boundary for filesystem operations.
 * Enforces workspace confinement, path traversal prevention, symlink escape checks,
 * and sensitive file redaction.
 */
@Component
public class WorkspaceGuard {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceGuard.class);

    private static final Set<String> BLOCKED_FILENAMES = Set.of(
            ".env", ".env.local", ".env.production", "id_rsa", "id_dsa",
            "id_ecdsa", "id_ed25519", ".bash_history", ".zsh_history"
    );

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            ".pem", ".key", ".p12", ".pfx", ".jks", ".keystore"
    );

    private final Path workspaceRoot;

    public WorkspaceGuard(@Value("${mcp.workspace.root:.}") String workspaceRootPath) {
        Path root = Paths.get(workspaceRootPath).toAbsolutePath().normalize();
        this.workspaceRoot = root;
        log.info("Initialized WorkspaceGuard with root: [{}]", this.workspaceRoot);
    }

    public Path getWorkspaceRoot() {
        return workspaceRoot;
    }

    /**
     * Resolves and validates that the requested path is strictly within the workspace root.
     * Prevents '../' escape, absolute path traversal outside workspace, symlink escape, and blocked files.
     */
    public Path validateAndResolve(String requestedPath) {
        if (requestedPath == null || requestedPath.isBlank()) {
            throw new IllegalArgumentException("Requested path must not be empty");
        }

        // Clean path of leading slashes if relative
        Path resolved = workspaceRoot.resolve(requestedPath).normalize();

        // 1. Path confinement check
        if (!resolved.startsWith(workspaceRoot)) {
            log.warn("Path traversal attempt blocked: [{}] resolved to [{}] outside root [{}]",
                    requestedPath, resolved, workspaceRoot);
            throw new SecurityException("Access denied: path is outside the allowed workspace boundary");
        }

        // 2. Symlink resolution check (if file exists)
        if (Files.exists(resolved)) {
            try {
                Path realPath = resolved.toRealPath();
                if (!realPath.startsWith(workspaceRoot)) {
                    log.warn("Symlink escape attempt blocked: [{}] points to [{}]", requestedPath, realPath);
                    throw new SecurityException("Access denied: symlink points outside workspace boundary");
                }
            } catch (IOException e) {
                throw new SecurityException("Access denied: cannot verify real path: " + e.getMessage());
            }
        }

        // 3. Block sensitive secret files
        String fileName = resolved.getFileName() != null ? resolved.getFileName().toString().toLowerCase() : "";
        if (BLOCKED_FILENAMES.contains(fileName)) {
            throw new SecurityException("Access denied: reading sensitive configuration file is blocked: " + fileName);
        }
        for (String ext : BLOCKED_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                throw new SecurityException("Access denied: reading cryptographic keys or keystores is blocked: " + fileName);
            }
        }

        return resolved;
    }
}
