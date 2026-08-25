package sumit.ai.ai_engineering.ai.tools.documentation;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.ai.tools.AiTool;
import sumit.ai.ai_engineering.ai.tools.documentation.TechnologyInfo.Database;
import sumit.ai.ai_engineering.ai.tools.documentation.TechnologyInfo.Framework;
import sumit.ai.ai_engineering.ai.tools.documentation.TechnologyInfo.Language;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;



@Component
public class DocumentationSearchTool implements AiTool {

    @Tool(description = "Search for documentation based on the provided technology and query")
    public DocumentationResult searchDocumentation(
        @ToolParam(description = "The technology to search documentation for") TechnologyInfo.Technology technology,
        @ToolParam(description = "The query to search for in the documentation") String query
    ){
        Language language = technology.language();
        Framework framework = technology.framework();
        Database database = technology.database();
        String title = "Documentation for " + language + ", " + framework + ", " + database;
        return new DocumentationResult(
            title,
            language.name(),
            framework.name(),
            database.name(),
            new TechnologyInfo.Urls(
                language.getLink(),
                framework.getLink(),
                database.getLink()
            )
        );
    }

    @Override
    public ToolsCategory category() {
        return ToolsCategory.DOCUMENTATION;
    }
}
