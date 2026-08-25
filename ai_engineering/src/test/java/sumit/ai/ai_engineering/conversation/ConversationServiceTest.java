package sumit.ai.ai_engineering.conversation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sumit.ai.ai_engineering.common.exception.ForbiddenAccessException;
import sumit.ai.ai_engineering.common.exception.ResourceNotFoundException;
import sumit.ai.ai_engineering.conversation.api.dto.ConversationDTO;
import sumit.ai.ai_engineering.conversation.api.dto.ConversationDetailDTO;
import sumit.ai.ai_engineering.conversation.api.dto.CreateConversationRequest;
import sumit.ai.ai_engineering.conversation.api.dto.MessageDTO;
import sumit.ai.ai_engineering.conversation.api.dto.UpdateConversationTitleRequest;
import sumit.ai.ai_engineering.conversation.application.ConversationServiceImpl;
import sumit.ai.ai_engineering.conversation.domain.Conversation;
import sumit.ai.ai_engineering.conversation.domain.ConversationStatus;
import sumit.ai.ai_engineering.conversation.domain.Message;
import sumit.ai.ai_engineering.conversation.domain.MessageRole;
import sumit.ai.ai_engineering.conversation.infrastructure.ConversationRepository;
import sumit.ai.ai_engineering.conversation.infrastructure.MessageRepository;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    private ConversationServiceImpl service;

    private final Long userId = 1L;
    private final UUID convId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ConversationServiceImpl(conversationRepository, messageRepository);
    }

    @Test
    void createConversation_savesAndReturnsDTO() {
        CreateConversationRequest req = new CreateConversationRequest("Test Chat");
        Conversation saved = Conversation.builder()
                .id(convId)
                .userId(userId)
                .title("Test Chat")
                .status(ConversationStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(conversationRepository.save(any(Conversation.class))).thenReturn(saved);

        ConversationDTO result = service.createConversation(userId, req);

        assertThat(result.id()).isEqualTo(convId);
        assertThat(result.title()).isEqualTo("Test Chat");
        assertThat(result.userId()).isEqualTo(userId);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void getUserConversations_returnsList() {
        Conversation c1 = Conversation.builder().id(UUID.randomUUID()).userId(userId).title("C1").build();
        Conversation c2 = Conversation.builder().id(UUID.randomUUID()).userId(userId).title("C2").build();

        when(conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId)).thenReturn(List.of(c1, c2));

        List<ConversationDTO> list = service.getUserConversations(userId);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).title()).isEqualTo("C1");
    }

    @Test
    void getConversation_ownedByUser_returnsDetailWithMessages() {
        Conversation conv = Conversation.builder()
                .id(convId)
                .userId(userId)
                .title("C1")
                .status(ConversationStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Message m1 = Message.builder()
                .id(UUID.randomUUID())
                .conversationId(convId)
                .role(MessageRole.USER)
                .content("Hello")
                .createdAt(LocalDateTime.now())
                .build();

        when(conversationRepository.findById(convId)).thenReturn(Optional.of(conv));
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(convId)).thenReturn(List.of(m1));

        ConversationDetailDTO detail = service.getConversation(userId, convId);

        assertThat(detail.id()).isEqualTo(convId);
        assertThat(detail.messages()).hasSize(1);
        assertThat(detail.messages().get(0).content()).isEqualTo("Hello");
    }

    @Test
    void getConversation_notOwnedByUser_throwsForbiddenAccess() {
        Conversation conv = Conversation.builder()
                .id(convId)
                .userId(999L) // other user
                .title("Private")
                .build();

        when(conversationRepository.findById(convId)).thenReturn(Optional.of(conv));

        assertThatThrownBy(() -> service.getConversation(userId, convId))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getConversation_notFound_throwsResourceNotFound() {
        when(conversationRepository.findById(convId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getConversation(userId, convId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTitle_ownedByUser_savesNewTitle() {
        Conversation conv = Conversation.builder()
                .id(convId)
                .userId(userId)
                .title("Old Title")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(conversationRepository.findById(convId)).thenReturn(Optional.of(conv));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(i -> i.getArgument(0));

        ConversationDTO updated = service.updateTitle(userId, convId, new UpdateConversationTitleRequest("New Title"));

        assertThat(updated.title()).isEqualTo("New Title");
        verify(conversationRepository).save(conv);
    }

    @Test
    void deleteConversation_ownedByUser_deletesMessagesAndConversation() {
        Conversation conv = Conversation.builder().id(convId).userId(userId).title("To Delete").build();
        when(conversationRepository.findById(convId)).thenReturn(Optional.of(conv));

        service.deleteConversation(userId, convId);

        verify(messageRepository).deleteByConversationId(convId);
        verify(conversationRepository).delete(conv);
    }

    @Test
    void appendMessage_ownedByUser_savesMessageAndUpdatesConversation() {
        Conversation conv = Conversation.builder()
                .id(convId)
                .userId(userId)
                .title("Chat")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Message savedMsg = Message.builder()
                .id(UUID.randomUUID())
                .conversationId(convId)
                .role(MessageRole.USER)
                .content("Hi")
                .tokenUsage(5)
                .createdAt(LocalDateTime.now())
                .build();

        when(conversationRepository.findById(convId)).thenReturn(Optional.of(conv));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMsg);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conv);

        MessageDTO result = service.appendMessage(userId, convId, MessageRole.USER, "Hi", 5);

        assertThat(result.content()).isEqualTo("Hi");
        assertThat(result.role()).isEqualTo(MessageRole.USER);
        verify(messageRepository).save(any(Message.class));
        verify(conversationRepository).save(conv);
    }

    @Test
    void getOrCreateConversation_existsAndOwned_returnsExisting() {
        Conversation conv = Conversation.builder().id(convId).userId(userId).title("Existing").build();
        when(conversationRepository.findById(convId)).thenReturn(Optional.of(conv));

        Conversation result = service.getOrCreateConversation(userId, convId, "Default");

        assertThat(result.getTitle()).isEqualTo("Existing");
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void getOrCreateConversation_existsAndNotOwned_throwsForbidden() {
        Conversation conv = Conversation.builder().id(convId).userId(888L).title("Other").build();
        when(conversationRepository.findById(convId)).thenReturn(Optional.of(conv));

        assertThatThrownBy(() -> service.getOrCreateConversation(userId, convId, "Default"))
                .isInstanceOf(ForbiddenAccessException.class);
    }
}
