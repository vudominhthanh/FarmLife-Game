package social_quest_service.dto;

import java.time.Instant;
import java.util.UUID;

public record GiftResponse(
        UUID giftId,
        UUID senderId,
        UUID receiverId,
        String itemCode,
        Integer quantity,
        String message,
        Boolean claimed,
        Instant claimedAt,
        Instant expiresAt,
        Instant createdAt
) {}
