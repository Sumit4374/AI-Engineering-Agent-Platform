package sumit.ai.ai_engineering.ai.tools.documentation;

import sumit.ai.ai_engineering.ai.tools.documentation.TechnologyInfo.Urls;

public record DocumentationResult(
    String title,
    String language,
    String framework,
    String database,
    Urls urls
) {}
