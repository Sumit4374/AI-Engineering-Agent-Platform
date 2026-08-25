package sumit.ai.ai_engineering.ai.tools.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sumit.ai.ai_engineering.ai.tools.model.HashAlgorithm;

class HashToolTest {

    private HashTool hashTool;

    @BeforeEach
    void setUp() {
        hashTool = new HashTool();
    }

    @Test
    void hash_md5_returnsExpectedHex() {
        // echo -n "hello" | md5sum => 5d41402abc4b2a76b9719d911017c592
        String result = hashTool.hash("hello", HashAlgorithm.MD5);
        assertThat(result).isEqualTo("5d41402abc4b2a76b9719d911017c592");
    }

    @Test
    void hash_sha256_returnsExpectedHex() {
        // echo -n "hello" | sha256sum
        String result = hashTool.hash("hello", HashAlgorithm.SHA_256);
        assertThat(result).isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
    }

    @Test
    void hash_sha512_returns128CharHex() {
        String result = hashTool.hash("hello", HashAlgorithm.SHA_512);
        assertThat(result).hasSize(128);
        assertThat(result).matches("[0-9a-f]+");
    }

    @Test
    void hash_emptyString_doesNotThrow() {
        assertThatCode(() -> hashTool.hash("", HashAlgorithm.MD5)).doesNotThrowAnyException();
    }

    @Test
    void hash_deterministicOutput_sameInputSameOutput() {
        String first  = hashTool.hash("test-input", HashAlgorithm.SHA_256);
        String second = hashTool.hash("test-input", HashAlgorithm.SHA_256);
        assertThat(first).isEqualTo(second);
    }

    @Test
    void hash_differentInputs_produceDifferentOutputs() {
        String a = hashTool.hash("input-a", HashAlgorithm.SHA_256);
        String b = hashTool.hash("input-b", HashAlgorithm.SHA_256);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void category_returnsDevelopment() {
        assertThat(hashTool.category().name()).isEqualTo("UTILITY");
    }
}
