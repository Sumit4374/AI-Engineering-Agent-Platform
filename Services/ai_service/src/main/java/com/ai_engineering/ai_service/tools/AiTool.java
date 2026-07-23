package com.ai_engineering.ai_service.tools;

import com.ai_engineering.ai_service.tools.model.ToolsCategory;

/**
 * Marker interface implemented by every tool bean so the {@link ToolRegistry}
 * can group tools by {@link ToolsCategory} and expose only the tools a given
 * capability requires.
 */
public interface AiTool {
    ToolsCategory category();
}
