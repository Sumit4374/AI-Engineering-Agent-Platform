package sumit.ai.ai_engineering.tool;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sumit.ai.ai_engineering.tool.filesystem.DirectoryTreeTool;
import sumit.ai.ai_engineering.tool.filesystem.FileMetadataTool;
import sumit.ai.ai_engineering.tool.filesystem.ProjectAnalyzerTool;
import sumit.ai.ai_engineering.tool.filesystem.ReadFileTool;
import sumit.ai.ai_engineering.tool.filesystem.SearchInProjectTool;
import sumit.ai.ai_engineering.tool.filesystem.WorkspaceGuard;

class FilesystemToolsTest {

    private WorkspaceGuard guard;
    private ReadFileTool readFileTool;
    private DirectoryTreeTool directoryTreeTool;
    private SearchInProjectTool searchTool;
    private FileMetadataTool metadataTool;
    private ProjectAnalyzerTool analyzerTool;

    @BeforeEach
    void setUp() {
        guard = new WorkspaceGuard(".");
        readFileTool = new ReadFileTool(guard);
        directoryTreeTool = new DirectoryTreeTool(guard);
        searchTool = new SearchInProjectTool(guard);
        metadataTool = new FileMetadataTool(guard);
        analyzerTool = new ProjectAnalyzerTool(guard);
    }

    @Test
    void readFileTool_readsPomXml() {
        String content = readFileTool.readFile("pom.xml", 20);
        assertThat(content).contains("<artifactId>ai_engineering</artifactId>");
    }

    @Test
    void readFileTool_blocksOutsideTraversal() {
        String content = readFileTool.readFile("../../etc/passwd", 10);
        assertThat(content).contains("Security Violation");
    }

    @Test
    void directoryTreeTool_generatesTreeStructure() {
        String tree = directoryTreeTool.tree("src/main/resources", 2);
        assertThat(tree).contains("prompts");
    }

    @Test
    void searchTool_findsMatchesInProject() {
        List<SearchInProjectTool.SearchResult> results = searchTool.search("spring-boot-starter", ".xml", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).file()).contains("pom.xml");
    }

    @Test
    void metadataTool_returnsFileFacts() {
        FileMetadataTool.FileMetadata meta = metadataTool.getMetadata("pom.xml");
        assertThat(meta.exists()).isTrue();
        assertThat(meta.isDirectory()).isFalse();
        assertThat(meta.sizeBytes()).isGreaterThan(0);
        assertThat(meta.extension()).isEqualTo(".xml");
    }

    @Test
    void analyzerTool_detectsJavaAndMaven() {
        ProjectAnalyzerTool.ProjectOverview overview = analyzerTool.analyzeProject();
        assertThat(overview.technologies()).contains("Maven / Java");
        assertThat(overview.keyConfigFiles()).contains("pom.xml");
    }
}
