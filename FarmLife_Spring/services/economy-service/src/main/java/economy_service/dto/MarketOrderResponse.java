package dto;

import java.time.Instant;
import java.util.UUID;

public record MarketOrderResponse(
        UUID orderId,
        UUID listingId,
        UUID buyerId,
        Integer quantity,
        Long unitPrice,
        Long totalPrice,
        Long tax,
        String status,
        Instant createdAt
) {}
