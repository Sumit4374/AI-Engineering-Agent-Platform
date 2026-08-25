package sumit.ai.ai_engineering.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sumit.ai.ai_engineering.memory.infrastructure.InMemoryMemoryStore;
import sumit.ai.ai_engineering.memory.model.MemoryMessage;

class InMemoryMemoryStoreTest {

    private InMemoryMemoryStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryMemoryStore();
    }

    @Test
    void saveAndGetRecentMessages_returnsStoredMessages() {
        store.saveMessage("c1", MemoryMessage.of("USER", "Hi"));
        store.saveMessage("c1", MemoryMessage.of("ASSISTANT", "Hello"));

        List<MemoryMessage> recent = store.getRecentMessages("c1", 10);

        assertThat(recent).hasSize(2);
        assertThat(recent.get(0).content()).isEqualTo("Hi");
        assertThat(recent.get(1).content()).isEqualTo("Hello");
    }

    @Test
    void getRecentMessages_limitApplied() {
        for (int i = 1; i <= 5; i++) {
            store.saveMessage("c1", MemoryMessage.of("USER", "Msg " + i));
        }

        List<MemoryMessage> recent = store.getRecentMessages("c1", 3);

        assertThat(recent).hasSize(3);
        assertThat(recent.get(0).content()).isEqualTo("Msg 3");
        assertThat(recent.get(2).content()).isEqualTo("Msg 5");
    }

    @Test
    void clear_removesConversation() {
        store.saveMessage("c1", MemoryMessage.of("USER", "Hi"));
        assertThat(store.exists("c1")).isTrue();

        store.clear("c1");

        assertThat(store.exists("c1")).isFalse();
        assertThat(store.getRecentMessages("c1", 10)).isEmpty();
    }

    @Test
    void setMessages_overwritesExisting() {
        store.saveMessage("c1", MemoryMessage.of("USER", "Old"));
        store.setMessages("c1", List.of(MemoryMessage.of("USER", "New 1"), MemoryMessage.of("ASSISTANT", "New 2")));

        List<MemoryMessage> messages = store.getRecentMessages("c1", 10);

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).content()).isEqualTo("New 1");
    }
}
