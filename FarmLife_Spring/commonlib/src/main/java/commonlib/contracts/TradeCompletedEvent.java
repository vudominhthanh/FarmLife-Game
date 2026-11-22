package contracts;

import java.time.Instant;
import java.util.UUID;

public record TradeCompletedEvent(
        UUID eventId,
        UUID listingId,
        UUID orderId,
        UUID sellerId,
        UUID buyerId,
        String itemCode,
        int quantity,
        long unitPrice,
        long totalPrice,
        long tax,
        Instant completedAt
) {}
