package com.ai_engineering.ai_service.tools.documentation;

import com.ai_engineering.ai_service.tools.documentation.TechnologyInfo.Urls;

public record DocumentationResult(
    String title,
    String language,
    String framework,
    String database,
    Urls urls
) {}
