package social_quest_service.controller;

import idempotency.Idempotent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import social_quest_service.dto.FriendRequestDto;
import social_quest_service.dto.FriendResponse;
import social_quest_service.service.FriendshipService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(@AuthenticationPrincipal String principal) {
        UUID playerId = UUID.fromString(principal);
        return ResponseEntity.ok(friendshipService.getFriends(playerId));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendResponse>> getPendingRequests(@AuthenticationPrincipal String principal) {
        UUID playerId = UUID.fromString(principal);
        return ResponseEntity.ok(friendshipService.getPendingRequests(playerId));
    }

    @PostMapping("/request")
    @Idempotent
    public ResponseEntity<FriendResponse> sendRequest(
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody FriendRequestDto request
    ) {
        UUID senderId = UUID.fromString(principal);
        return ResponseEntity.ok(friendshipService.sendFriendRequest(senderId, request.targetPlayerId()));
    }

    @PostMapping("/{friendId}/accept")
    @Idempotent
    public ResponseEntity<FriendResponse> acceptRequest(
            @AuthenticationPrincipal String principal,
            @PathVariable UUID friendId
    ) {
        UUID playerId = UUID.fromString(principal);
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(playerId, friendId));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> declineOrRemove(
            @AuthenticationPrincipal String principal,
            @PathVariable UUID friendId
    ) {
        UUID playerId = UUID.fromString(principal);
        friendshipService.declineOrRemoveFriend(playerId, friendId);
        return ResponseEntity.noContent().build();
    }
}
