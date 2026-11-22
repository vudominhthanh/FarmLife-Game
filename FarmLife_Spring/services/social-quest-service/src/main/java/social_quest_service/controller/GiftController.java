package social_quest_service.controller;

import idempotency.Idempotent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import social_quest_service.dto.GiftResponse;
import social_quest_service.dto.SendGiftRequest;
import social_quest_service.service.GiftService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gifts")
@RequiredArgsConstructor
public class GiftController {

    private final GiftService giftService;

    @PostMapping
    @Idempotent
    public ResponseEntity<GiftResponse> sendGift(
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody SendGiftRequest request
    ) {
        UUID senderId = UUID.fromString(principal);
        return ResponseEntity.ok(giftService.sendGift(senderId, request));
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<GiftResponse>> getIncomingGifts(@AuthenticationPrincipal String principal) {
        UUID receiverId = UUID.fromString(principal);
        return ResponseEntity.ok(giftService.getIncomingGifts(receiverId));
    }

    @PostMapping("/{giftId}/claim")
    @Idempotent
    public ResponseEntity<GiftResponse> claimGift(
            @AuthenticationPrincipal String principal,
            @PathVariable UUID giftId
    ) {
        UUID receiverId = UUID.fromString(principal);
        return ResponseEntity.ok(giftService.claimGift(receiverId, giftId));
    }
}
