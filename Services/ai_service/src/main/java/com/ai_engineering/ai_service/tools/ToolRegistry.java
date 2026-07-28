package com.ai_engineering.ai_service.tools;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.tools.model.ToolsCategory;


/**
 * Central registry of all tool beans, grouped by {@link ToolsCategory}.
 *
 * <p>The AI Engine asks the registry for the tools required by the current
 * capability, so the LLM is only ever offered a scoped set of tools instead of
 * every tool in the application.
 */
@Component
public class ToolRegistry {

    private final Map<ToolsCategory, List<Object>> toolsByCategory;

    
    public ToolRegistry(List<AiTool> tools) {
        this.toolsByCategory = tools.stream()
                .collect(Collectors.groupingBy(
                        tool -> tool.category(),
                        Collectors.mapping(tool -> (Object) tool, Collectors.toList())));
    }

    /**
     * Returns the tool beans for the requested categories, ready to hand to
     * {@code ChatClient.prompt().tools(...)}. Unknown/empty categories simply
     * contribute nothing.
     */
    public Object[] getTools(ToolsCategory... categories) {
        return Arrays.stream(categories)
                .flatMap(category -> toolsByCategory.getOrDefault(category, List.of()).stream())
                .toArray();
    }
}
