package sumit.ai.ai_engineering.conversation.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import sumit.ai.ai_engineering.common.utility.SecurityUtils;
import sumit.ai.ai_engineering.conversation.api.dto.AppendMessageRequest;
import sumit.ai.ai_engineering.conversation.api.dto.ConversationDTO;
import sumit.ai.ai_engineering.conversation.api.dto.ConversationDetailDTO;
import sumit.ai.ai_engineering.conversation.api.dto.CreateConversationRequest;
import sumit.ai.ai_engineering.conversation.api.dto.MessageDTO;
import sumit.ai.ai_engineering.conversation.api.dto.UpdateConversationTitleRequest;
import sumit.ai.ai_engineering.conversation.application.ConversationService;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(@Valid @RequestBody CreateConversationRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        ConversationDTO created = conversationService.createConversation(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ConversationDTO>> listConversations() {
        Long userId = SecurityUtils.getRequiredUserId();
        return ResponseEntity.ok(conversationService.getUserConversations(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDetailDTO> getConversation(@PathVariable("id") UUID id) {
        Long userId = SecurityUtils.getRequiredUserId();
        return ResponseEntity.ok(conversationService.getConversation(userId, id));
    }

    @PatchMapping("/{id}/title")
    public ResponseEntity<ConversationDTO> updateTitle(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateConversationTitleRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        return ResponseEntity.ok(conversationService.updateTitle(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable("id") UUID id) {
        Long userId = SecurityUtils.getRequiredUserId();
        conversationService.deleteConversation(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable("id") UUID id) {
        Long userId = SecurityUtils.getRequiredUserId();
        return ResponseEntity.ok(conversationService.getMessages(userId, id));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageDTO> appendMessage(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AppendMessageRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        MessageDTO message = conversationService.appendMessage(
                userId, id, request.role(), request.content(), request.tokenUsage());
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
}
