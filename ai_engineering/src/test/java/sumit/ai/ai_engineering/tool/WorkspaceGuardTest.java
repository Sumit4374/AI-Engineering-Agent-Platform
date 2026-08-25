package sumit.ai.ai_engineering.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sumit.ai.ai_engineering.tool.filesystem.WorkspaceGuard;

class WorkspaceGuardTest {

    private WorkspaceGuard guard;

    @BeforeEach
    void setUp() {
        guard = new WorkspaceGuard(".");
    }

    @Test
    void validateAndResolve_validRelativePath_resolvesInsideRoot() {
        Path resolved = guard.validateAndResolve("pom.xml");
        assertThat(resolved).exists();
        assertThat(resolved.getFileName().toString()).isEqualTo("pom.xml");
    }

    @Test
    void validateAndResolve_pathTraversalAttempt_throwsSecurityException() {
        assertThatThrownBy(() -> guard.validateAndResolve("../../etc/passwd"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void validateAndResolve_blockedEnvFile_throwsSecurityException() {
        assertThatThrownBy(() -> guard.validateAndResolve(".env"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void validateAndResolve_blockedKeyFile_throwsSecurityException() {
        assertThatThrownBy(() -> guard.validateAndResolve("certs/private.key"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void validateAndResolve_emptyPath_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> guard.validateAndResolve(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
