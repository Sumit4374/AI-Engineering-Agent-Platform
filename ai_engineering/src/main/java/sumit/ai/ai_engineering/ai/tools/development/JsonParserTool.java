package sumit.ai.ai_engineering.ai.tools.development;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sumit.ai.ai_engineering.ai.tools.AiTool;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;

/**
 * JSON parsing and inspection tool.
 *
 * <p>Provides deterministic JSON operations using Jackson: validation, key extraction,
 * and path-based value lookup. The LLM uses this tool to inspect JSON structure and values
 * before reasoning about them.
 *
 * <p>Security: input is never executed or evaluated as code. Input is capped to 64 KB.
 */
@Component
public class JsonParserTool implements AiTool {

    private static final int MAX_INPUT_LENGTH = 65_536; // 64 KB
    private final ObjectMapper objectMapper;

    public JsonParserTool() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Validates whether the given string is well-formed JSON.
     */
    @Tool(description = "Validate whether a string is well-formed JSON. Returns true if valid, false otherwise.")
    public boolean validate(
            @ToolParam(description = "The JSON string to validate (max 64 KB)") String json) {
        if (json == null || json.isBlank()) return false;
        try {
            objectMapper.readTree(truncate(json));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns a list of all top-level field names in a JSON object.
     */
    @Tool(description = """
            Extract the top-level field names (keys) from a JSON object.
            Returns an empty list if the JSON is not an object or is invalid.
            """)
    public List<String> extractKeys(
            @ToolParam(description = "The JSON object string to extract keys from (max 64 KB)") String json) {
        List<String> keys = new ArrayList<>();
        try {
            JsonNode node = objectMapper.readTree(truncate(json));
            if (node.isObject()) {
                node.fieldNames().forEachRemaining(keys::add);
            }
        } catch (Exception ignored) {
            // Return empty list on parse failure
        }
        return keys;
    }

    /**
     * Looks up a value at a JSON Pointer path.
     *
     * <p>JSON Pointer syntax: {@code /field/nestedField/0} (RFC 6901).
     * Returns an empty string if the path does not exist or the JSON is invalid.
     */
    @Tool(description = """
            Look up a value in a JSON structure using a JSON Pointer path (RFC 6901 format).
            Examples: '/name', '/address/city', '/items/0/price'.
            Returns the value as a string, or empty string if the path does not exist.
            """)
    public String getValueAtPath(
            @ToolParam(description = "The JSON string to search (max 64 KB)") String json,
            @ToolParam(description = "The JSON Pointer path, e.g. '/user/name' or '/items/0'") String path) {
        try {
            JsonNode root = objectMapper.readTree(truncate(json));
            JsonNode target = root.at(path);
            if (target.isMissingNode()) return "";
            return target.isValueNode() ? target.asText() : target.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns the structural type of the JSON value at the given path.
     */
    @Tool(description = """
            Returns the JSON node type at the given JSON Pointer path.
            Possible values: OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL, MISSING, INVALID.
            """)
    public String getTypeAtPath(
            @ToolParam(description = "The JSON string to inspect (max 64 KB)") String json,
            @ToolParam(description = "The JSON Pointer path to inspect, e.g. '/items'") String path) {
        try {
            JsonNode root = objectMapper.readTree(truncate(json));
            JsonNode target = root.at(path);
            if (target.isMissingNode()) return "MISSING";
            return target.getNodeType().name();
        } catch (JsonProcessingException e) {
            return "INVALID";
        }
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    private String truncate(String input) {
        return (input != null && input.length() > MAX_INPUT_LENGTH)
                ? input.substring(0, MAX_INPUT_LENGTH)
                : input;
    }

    @Override
    public ToolsCategory category() {
        return ToolsCategory.DEVELOPMENT;
    }
}
