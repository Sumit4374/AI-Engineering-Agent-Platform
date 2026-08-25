package sumit.ai.ai_engineering.tool.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.ai.tools.AiTool;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

@Component
public class ProjectAnalyzerTool implements AiTool {

    private final WorkspaceGuard workspaceGuard;

    public ProjectAnalyzerTool(WorkspaceGuard workspaceGuard) {
        this.workspaceGuard = workspaceGuard;
    }

    @Tool(description = "Analyze the project structure, detect build tools, frameworks, and key configuration files.")
    public ProjectOverview analyzeProject() {
        Path root = workspaceGuard.getWorkspaceRoot();
        List<String> detectedTechnologies = new ArrayList<>();
        List<String> keyFilesFound = new ArrayList<>();

        checkFile(root, "pom.xml", "Maven / Java", detectedTechnologies, keyFilesFound);
        checkFile(root, "build.gradle", "Gradle / Java-Kotlin", detectedTechnologies, keyFilesFound);
        checkFile(root, "package.json", "Node.js / JavaScript / TypeScript", detectedTechnologies, keyFilesFound);
        checkFile(root, "requirements.txt", "Python (pip)", detectedTechnologies, keyFilesFound);
        checkFile(root, "pyproject.toml", "Python (modern)", detectedTechnologies, keyFilesFound);
        checkFile(root, "Dockerfile", "Docker Container", detectedTechnologies, keyFilesFound);
        checkFile(root, "docker-compose.yml", "Docker Compose", detectedTechnologies, keyFilesFound);
        checkFile(root, "src/main/resources/application.properties", "Spring Boot Configuration", detectedTechnologies, keyFilesFound);
        checkFile(root, "src/main/resources/application.yml", "Spring Boot YAML Configuration", detectedTechnologies, keyFilesFound);

        String projectName = root.getFileName() != null ? root.getFileName().toString() : "workspace";

        return new ProjectOverview(
                projectName,
                detectedTechnologies,
                keyFilesFound
        );
    }

    private void checkFile(Path root, String relativePath, String techName, List<String> techList, List<String> filesList) {
        Path p = root.resolve(relativePath);
        if (Files.exists(p)) {
            techList.add(techName);
            filesList.add(relativePath);
        }
    }

    public record ProjectOverview(
            String projectName,
            List<String> technologies,
            List<String> keyConfigFiles
    ) {}

    @Override
    public ToolsCategory category() {
        return ToolsCategory.PROJECT;
    }
}
