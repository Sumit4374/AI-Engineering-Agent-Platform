package sumit.ai.ai_engineering.ai.tools.development;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegexGeneratorToolTest {

    private RegexGeneratorTool tool;

    @BeforeEach
    void setUp() {
        tool = new RegexGeneratorTool();
    }

    // ---- emailRegex ----

    @Test
    void emailRegex_returnsNonBlankPattern() {
        assertThat(tool.emailRegex()).isNotBlank();
    }

    @Test
    void emailRegex_matchesValidEmail() {
        String regex = tool.emailRegex();
        assertThat(tool.matches(regex, "user@example.com")).isTrue();
    }

    @Test
    void emailRegex_rejectsEmailWithoutAtSign() {
        String regex = tool.emailRegex();
        assertThat(tool.matches(regex, "notanemail")).isFalse();
    }

    // ---- phoneRegex ----

    @Test
    void phoneRegex_returnsNonBlankPattern() {
        assertThat(tool.phoneRegex()).isNotBlank();
    }

    // ---- isvalid ----

    @Test
    void isvalid_wellFormedRegex_returnsTrue() {
        assertThat(tool.isvalid("^[A-Z]+$")).isTrue();
    }

    @Test
    void isvalid_malformedRegex_returnsFalse() {
        assertThat(tool.isvalid("[unclosed")).isFalse();
    }

    // ---- matches ----

    @Test
    void matches_patternMatchesInput_returnsTrue() {
        assertThat(tool.matches("\\d+", "12345")).isTrue();
    }

    @Test
    void matches_patternDoesNotMatchInput_returnsFalse() {
        assertThat(tool.matches("\\d+", "abc")).isFalse();
    }

    @Test
    void matches_invalidRegex_returnsFalse() {
        assertThat(tool.matches("[broken", "test")).isFalse();
    }

    // ---- matchers ----

    @Test
    void matchers_findsAllOccurrences() {
        List<String> results = tool.matchers("\\d+", "abc 12 def 34 ghi 56");
        assertThat(results).containsExactly("12", "34", "56");
    }

    @Test
    void matchers_noMatches_returnsEmptyList() {
        List<String> results = tool.matchers("\\d+", "no digits here");
        assertThat(results).isEmpty();
    }

    @Test
    void matchers_invalidRegex_returnsEmptyList() {
        List<String> results = tool.matchers("[invalid", "input");
        assertThat(results).isEmpty();
    }

    // ---- category ----

    @Test
    void category_returnsDevelopment() {
        assertThat(tool.category().name()).isEqualTo("DEVELOPMENT");
    }
}
