package sumit.ai.ai_engineering.ai.tools.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.ai.tools.AiTool;
import sumit.ai.ai_engineering.ai.tools.model.HashAlgorithm;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;



@Component
public class HashTool implements AiTool {
    
    @Tool(description = "Compute the hexadecimal hash of the given input using the specified algorithm")
    public String hash(
        @ToolParam(description = "String Input for the hexadecimal Hashing") String input,
        @ToolParam(description = "Type of the algorithm from (MD5, SHA_256, SHA_512)") HashAlgorithm algorithm){
        try {
            String name = switch(algorithm){
                case MD5 -> "MD5";
                case SHA_256 -> "SHA-256";
                case SHA_512 -> "SHA-512";
            };
            MessageDigest digest = MessageDigest.getInstance(name);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public ToolsCategory category() {
        return ToolsCategory.UTILITY;
    }
}
