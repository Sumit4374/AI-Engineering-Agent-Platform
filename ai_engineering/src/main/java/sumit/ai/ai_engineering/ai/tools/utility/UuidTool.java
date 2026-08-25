package sumit.ai.ai_engineering.ai.tools.utility;

import java.util.UUID;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.ai.tools.AiTool;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;


@Component
public class UuidTool implements AiTool {

    @Tool(description = "Generate a random UUID (version 4) and return it as a java.util.UUID.")
    public UUID generate() {
        return UUID.randomUUID();
    }

    @Override
    public ToolsCategory category() {
        return ToolsCategory.UTILITY;
    }
}
