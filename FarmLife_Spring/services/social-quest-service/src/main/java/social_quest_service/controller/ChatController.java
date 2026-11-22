package social_quest_service.controller;

import idempotency.Idempotent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import social_quest_service.dto.ChatMessageResponse;
import social_quest_service.dto.SendChatMessageRequest;
import social_quest_service.service.ChatService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @Idempotent
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody SendChatMessageRequest request
    ) {
        UUID senderId = UUID.fromString(principal);
        return ResponseEntity.ok(chatService.sendMessage(senderId, request));
    }

    @GetMapping("/{channel}")
    public ResponseEntity<List<ChatMessageResponse>> getRecentMessages(@PathVariable String channel) {
        return ResponseEntity.ok(chatService.getRecentMessages(channel));
    }
}
