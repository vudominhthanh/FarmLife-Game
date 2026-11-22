package dto;

import java.time.Instant;
import java.util.UUID;

public record MarketListingResponse(
        UUID listingId,
        UUID sellerId,
        String itemCode,
        Integer initialQuantity,
        Integer remainingQuantity,
        Long unitPrice,
        String status,
        Instant expiresAt,
        Instant createdAt
) {}
