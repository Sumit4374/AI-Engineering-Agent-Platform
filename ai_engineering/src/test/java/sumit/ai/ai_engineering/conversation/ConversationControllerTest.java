package sumit.ai.ai_engineering.conversation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import sumit.ai.ai_engineering.conversation.api.ConversationController;
import sumit.ai.ai_engineering.conversation.api.dto.AppendMessageRequest;
import sumit.ai.ai_engineering.conversation.api.dto.ConversationDTO;
import sumit.ai.ai_engineering.conversation.api.dto.ConversationDetailDTO;
import sumit.ai.ai_engineering.conversation.api.dto.CreateConversationRequest;
import sumit.ai.ai_engineering.conversation.api.dto.MessageDTO;
import sumit.ai.ai_engineering.conversation.api.dto.UpdateConversationTitleRequest;
import sumit.ai.ai_engineering.conversation.application.ConversationService;
import sumit.ai.ai_engineering.conversation.domain.ConversationStatus;
import sumit.ai.ai_engineering.conversation.domain.MessageRole;
import sumit.ai.ai_engineering.user.Configuration.CustomUserDetails.CustomUserDetails;
import sumit.ai.ai_engineering.user.Model.User;
import sumit.ai.ai_engineering.user.Model.Enum.Role;

@ExtendWith(MockitoExtension.class)
class ConversationControllerTest {

    @Mock
    private ConversationService conversationService;

    private ConversationController controller;

    private final Long userId = 10L;
    private final UUID convId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new ConversationController(conversationService);
        User user = User.builder().id(userId).userName("testuser").role(Role.USER).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createConversation_returnsCreatedStatus() {
        CreateConversationRequest req = new CreateConversationRequest("New Topic");
        ConversationDTO dto = new ConversationDTO(convId, userId, "New Topic", ConversationStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());

        when(conversationService.createConversation(eq(userId), any())).thenReturn(dto);

        ResponseEntity<ConversationDTO> resp = controller.createConversation(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().title()).isEqualTo("New Topic");
    }

    @Test
    void listConversations_returnsOk() {
        when(conversationService.getUserConversations(userId)).thenReturn(List.of());

        ResponseEntity<List<ConversationDTO>> resp = controller.listConversations();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEmpty();
    }

    @Test
    void getConversation_returnsDetail() {
        ConversationDetailDTO detail = new ConversationDetailDTO(
                convId, userId, "Title", ConversationStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(conversationService.getConversation(userId, convId)).thenReturn(detail);

        ResponseEntity<ConversationDetailDTO> resp = controller.getConversation(convId);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().id()).isEqualTo(convId);
    }

    @Test
    void updateTitle_returnsUpdatedDTO() {
        ConversationDTO updated = new ConversationDTO(convId, userId, "Updated", ConversationStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        when(conversationService.updateTitle(eq(userId), eq(convId), any())).thenReturn(updated);

        ResponseEntity<ConversationDTO> resp = controller.updateTitle(convId, new UpdateConversationTitleRequest("Updated"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().title()).isEqualTo("Updated");
    }

    @Test
    void deleteConversation_returnsNoContent() {
        ResponseEntity<Void> resp = controller.deleteConversation(convId);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(conversationService).deleteConversation(userId, convId);
    }

    @Test
    void appendMessage_returnsCreated() {
        AppendMessageRequest req = new AppendMessageRequest(MessageRole.USER, "Hello", null);
        MessageDTO msg = new MessageDTO(UUID.randomUUID(), convId, MessageRole.USER, "Hello", 5, LocalDateTime.now());

        when(conversationService.appendMessage(eq(userId), eq(convId), eq(MessageRole.USER), eq("Hello"), any()))
                .thenReturn(msg);

        ResponseEntity<MessageDTO> resp = controller.appendMessage(convId, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().content()).isEqualTo("Hello");
    }
}
