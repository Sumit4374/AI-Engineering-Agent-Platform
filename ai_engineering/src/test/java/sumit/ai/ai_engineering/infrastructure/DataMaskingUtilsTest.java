package sumit.ai.ai_engineering.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import sumit.ai.ai_engineering.infrastructure.security.DataMaskingUtils;

class DataMaskingUtilsTest {

    @Test
    void maskSensitiveData_masksBearerToken() {
        String logMessage = "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIn0.xyz";
        String masked = DataMaskingUtils.maskSensitiveData(logMessage);

        assertThat(masked).doesNotContain("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        assertThat(masked).contains("Bearer [REDACTED_JWT]");
    }

    @Test
    void maskSensitiveData_masksApiKey() {
        String logMessage = "Connecting with api_key=nvapi-1234567890abcdef for model";
        String masked = DataMaskingUtils.maskSensitiveData(logMessage);

        assertThat(masked).doesNotContain("nvapi-1234567890abcdef");
        assertThat(masked).contains("api_key=[REDACTED]");
    }

    @Test
    void maskApiKey_shortensToPrefixAndSuffix() {
        String key = "nvapi-1234567890abcdef";
        String masked = DataMaskingUtils.maskApiKey(key);

        assertThat(masked).isEqualTo("nvap...cdef");
    }
}
