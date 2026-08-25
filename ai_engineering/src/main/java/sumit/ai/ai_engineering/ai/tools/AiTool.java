package sumit.ai.ai_engineering.ai.tools;

import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

/**
 * Marker interface implemented by every tool bean so the {@link ToolRegistry}
 * can group tools by {@link ToolsCategory} and expose only the tools a given
 * capability requires.
 */
public interface AiTool {
    ToolsCategory category();
}
