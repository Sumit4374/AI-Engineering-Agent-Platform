package sumit.ai.ai_engineering.memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sumit.ai.ai_engineering.conversation.domain.Message;
import sumit.ai.ai_engineering.conversation.domain.MessageRole;
import sumit.ai.ai_engineering.conversation.infrastructure.MessageRepository;
import sumit.ai.ai_engineering.memory.application.ConversationMemoryServiceImpl;
import sumit.ai.ai_engineering.memory.infrastructure.MemoryStore;
import sumit.ai.ai_engineering.memory.model.ConversationContext;
import sumit.ai.ai_engineering.memory.model.MemoryMessage;

@ExtendWith(MockitoExtension.class)
class ConversationMemoryServiceTest {

    @Mock
    private MemoryStore memoryStore;

    @Mock
    private MessageRepository messageRepository;

    private ConversationMemoryServiceImpl service;

    private final String convId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        service = new ConversationMemoryServiceImpl(memoryStore, messageRepository);
    }

    @Test
    void getBoundedContext_emptyOrNullId_returnsEmptyContext() {
        ConversationContext ctx = service.getBoundedContext("", 1000, 10);
        assertThat(ctx.messages()).isEmpty();
        assertThat(ctx.totalTokens()).isEqualTo(0);
    }

    @Test
    void getBoundedContext_fromMemoryStore_appliesMessageAndTokenLimits() {
        List<MemoryMessage> cached = List.of(
                new MemoryMessage("USER", "First message", 10),
                new MemoryMessage("ASSISTANT", "Second message", 10),
                new MemoryMessage("USER", "Third message", 10),
                new MemoryMessage("ASSISTANT", "Fourth message", 10)
        );

        when(memoryStore.exists(convId)).thenReturn(true);
        when(memoryStore.getRecentMessages(convId, 50)).thenReturn(cached);

        // Limit to 2 max messages
        ConversationContext ctx = service.getBoundedContext(convId, 1000, 2);

        assertThat(ctx.messages()).hasSize(2);
        assertThat(ctx.messages().get(0).content()).isEqualTo("Third message");
        assertThat(ctx.messages().get(1).content()).isEqualTo("Fourth message");
        assertThat(ctx.totalTokens()).isEqualTo(20);
    }

    @Test
    void getBoundedContext_tokenBudgetEnforced() {
        List<MemoryMessage> cached = List.of(
                new MemoryMessage("USER", "Small 1", 50),
                new MemoryMessage("ASSISTANT", "Small 2", 50),
                new MemoryMessage("USER", "Large 3", 80)
        );

        when(memoryStore.exists(convId)).thenReturn(true);
        when(memoryStore.getRecentMessages(convId, 50)).thenReturn(cached);

        // Max 100 tokens: Large 3 (80 tokens) fits; adding Small 2 (+50 = 130) exceeds 100 budget
        ConversationContext ctx = service.getBoundedContext(convId, 100, 10);

        assertThat(ctx.messages()).hasSize(1);
        assertThat(ctx.messages().get(0).content()).isEqualTo("Large 3");
    }

    @Test
    void getBoundedContext_coldStartHydratesFromDatabase() {
        UUID convUuid = UUID.fromString(convId);
        Message m1 = Message.builder()
                .id(UUID.randomUUID())
                .conversationId(convUuid)
                .role(MessageRole.USER)
                .content("DB User msg")
                .tokenUsage(8)
                .createdAt(LocalDateTime.now())
                .build();
        Message m2 = Message.builder()
                .id(UUID.randomUUID())
                .conversationId(convUuid)
                .role(MessageRole.ASSISTANT)
                .content("DB Assistant reply")
                .tokenUsage(12)
                .createdAt(LocalDateTime.now())
                .build();

        when(memoryStore.exists(convId)).thenReturn(false);
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(convUuid)).thenReturn(List.of(m1, m2));

        ConversationContext ctx = service.getBoundedContext(convId, 500, 10);

        assertThat(ctx.messages()).hasSize(2);
        assertThat(ctx.messages().get(0).content()).isEqualTo("DB User msg");
        assertThat(ctx.messages().get(1).content()).isEqualTo("DB Assistant reply");
        verify(memoryStore).setMessages(any(), any());
    }

    @Test
    void recordMessage_savesToMemoryStore() {
        service.recordMessage(convId, "USER", "New message");

        verify(memoryStore).saveMessage(any(), any(MemoryMessage.class));
    }

    @Test
    void clearMemory_clearsMemoryStore() {
        service.clearMemory(convId);

        verify(memoryStore).clear(convId);
    }
}
