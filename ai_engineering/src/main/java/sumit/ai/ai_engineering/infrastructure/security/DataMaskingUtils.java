package sumit.ai.ai_engineering.infrastructure.security;

import java.util.regex.Pattern;

/**
 * Sanitization utility for structured logging and audit safety.
 * Masks bearer tokens, passwords, API keys, and sensitive authorization headers.
 */
public final class DataMaskingUtils {

    private static final Pattern BEARER_PATTERN = Pattern.compile("Bearer\\s+[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*", Pattern.CASE_INSENSITIVE);
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(?i)(api[_-]?key|secret|password|token)[\"']?\\s*[:=]\\s*[\"']?([^\"'\\s,]+)[\"']?");

    private DataMaskingUtils() {}

    public static String maskSensitiveData(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String masked = BEARER_PATTERN.matcher(input).replaceAll("Bearer [REDACTED_JWT]");
        masked = API_KEY_PATTERN.matcher(masked).replaceAll("$1=[REDACTED]");
        return masked;
    }

    public static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "[REDACTED]";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
