package dto;

import java.time.Instant;
import java.util.UUID;

public record MailboxItemResponse(
        UUID mailItemId,
        UUID playerId,
        String itemCode,
        Integer quantity,
        Integer quality,
        String sourceType,
        Instant expiresAt,
        Instant createdAt
) {}
