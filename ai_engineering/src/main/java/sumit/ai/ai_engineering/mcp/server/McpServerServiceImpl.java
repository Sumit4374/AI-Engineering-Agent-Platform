package sumit.ai.ai_engineering.mcp.server;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import sumit.ai.ai_engineering.ai.tools.AiTool;
import sumit.ai.ai_engineering.mcp.model.McpServerInfo;
import sumit.ai.ai_engineering.mcp.model.McpToolCallRequest;
import sumit.ai.ai_engineering.mcp.model.McpToolCallResult;
import sumit.ai.ai_engineering.mcp.model.McpToolDefinition;

@Service
public class McpServerServiceImpl implements McpServerService {

    private static final Logger log = LoggerFactory.getLogger(McpServerServiceImpl.class);
    private static final String SERVER_NAME = "ai-engineering-mcp-server";
    private static final String SERVER_VERSION = "1.0.0";
    private static final String PROTOCOL_VERSION = "2024-11-05";

    private final List<AiTool> registeredTools;
    private final Map<String, ToolInvoker> invokerMap = new HashMap<>();

    public McpServerServiceImpl(List<AiTool> registeredTools) {
        this.registeredTools = registeredTools != null ? registeredTools : List.of();
        initializeInvokers();
    }

    private void initializeInvokers() {
        for (AiTool toolBean : registeredTools) {
            for (Method method : toolBean.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Tool.class)) {
                    Tool toolAnnotation = method.getAnnotation(Tool.class);
                    String name = method.getName();
                    String description = toolAnnotation.description();
                    invokerMap.put(name.toLowerCase(), new ToolInvoker(toolBean, method, name, description, toolBean.category().name()));
                    log.debug("Registered MCP tool [name={}, bean={}]", name, toolBean.getClass().getSimpleName());
                }
            }
        }
        log.info("MCP Server initialized with {} tools", invokerMap.size());
    }

    @Override
    public McpServerInfo getServerInfo() {
        return new McpServerInfo(
                SERVER_NAME,
                SERVER_VERSION,
                PROTOCOL_VERSION,
                invokerMap.size(),
                List.of("tools", "filesystem", "utility", "development", "documentation")
        );
    }

    @Override
    public List<McpToolDefinition> listTools() {
        List<McpToolDefinition> definitions = new ArrayList<>();
        for (ToolInvoker invoker : invokerMap.values()) {
            Map<String, Object> schema = new HashMap<>();
            Map<String, Object> properties = new HashMap<>();
            List<String> required = new ArrayList<>();

            for (Parameter param : invoker.method().getParameters()) {
                Map<String, Object> paramMeta = new HashMap<>();
                paramMeta.put("type", getJsonType(param.getType()));
                if (param.isAnnotationPresent(ToolParam.class)) {
                    ToolParam tp = param.getAnnotation(ToolParam.class);
                    paramMeta.put("description", tp.description());
                }
                properties.put(param.getName(), paramMeta);
                if (param.getType().isPrimitive()) {
                    required.add(param.getName());
                }
            }

            schema.put("type", "object");
            schema.put("properties", properties);
            if (!required.isEmpty()) {
                schema.put("required", required);
            }

            definitions.add(new McpToolDefinition(
                    invoker.name(),
                    invoker.description(),
                    invoker.category(),
                    schema
            ));
        }
        return definitions;
    }

    @Override
    public McpToolCallResult executeTool(McpToolCallRequest request) {
        if (request == null || request.toolName() == null || request.toolName().isBlank()) {
            return McpToolCallResult.error("unknown", "Tool name must not be blank");
        }

        ToolInvoker invoker = invokerMap.get(request.toolName().toLowerCase());
        if (invoker == null) {
            return McpToolCallResult.error(request.toolName(), "Unknown tool: " + request.toolName());
        }

        try {
            Object[] args = resolveArguments(invoker.method(), request.arguments());
            Object result = invoker.method().invoke(invoker.bean(), args);
            return McpToolCallResult.success(invoker.name(), result);
        } catch (Exception e) {
            log.error("Error executing MCP tool [{}]: {}", request.toolName(), e.getMessage(), e);
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            return McpToolCallResult.error(request.toolName(), "Tool execution failed: " + message);
        }
    }

    private Object[] resolveArguments(Method method, Map<String, Object> argMap) {
        Parameter[] parameters = method.getParameters();
        Object[] resolved = new Object[parameters.length];
        Map<String, Object> safeMap = argMap != null ? argMap : Map.of();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Object val = safeMap.get(param.getName());
            if (val == null) {
                // Try case-insensitive matching
                for (Map.Entry<String, Object> e : safeMap.entrySet()) {
                    if (e.getKey().equalsIgnoreCase(param.getName())) {
                        val = e.getValue();
                        break;
                    }
                }
            }
            resolved[i] = convertValue(val, param.getType());
        }
        return resolved;
    }

    private Object convertValue(Object val, Class<?> targetType) {
        if (val == null) {
            if (targetType.isPrimitive()) {
                if (targetType == int.class) return 0;
                if (targetType == double.class) return 0.0;
                if (targetType == boolean.class) return false;
            }
            return null;
        }

        if (targetType.isAssignableFrom(val.getClass())) {
            return val;
        }

        String str = val.toString();
        if (targetType == String.class) return str;
        if (targetType == Integer.class || targetType == int.class) {
            try { return (int) Double.parseDouble(str); } catch (Exception e) { return 0; }
        }
        if (targetType == Double.class || targetType == double.class) {
            try { return Double.parseDouble(str); } catch (Exception e) { return 0.0; }
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(str);
        }

        return val;
    }

    private String getJsonType(Class<?> type) {
        if (type == String.class) return "string";
        if (type == int.class || type == Integer.class || type == long.class || type == Long.class) return "integer";
        if (type == double.class || type == Double.class || type == float.class || type == Float.class) return "number";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        return "object";
    }

    private record ToolInvoker(Object bean, Method method, String name, String description, String category) {}
}
