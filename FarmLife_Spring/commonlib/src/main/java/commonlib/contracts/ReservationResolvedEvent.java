package contracts;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResolvedEvent(
        UUID eventId,
        UUID reservationId,
        UUID playerId,
        String reason,
        UUID refId,
        boolean success,
        String failureReason,
        List<ItemQuantity> items,
        Instant resolvedAt
) {
    public ReservationResolvedEvent(
            UUID reservationId,
            UUID playerId,
            String reason,
            UUID refId,
            boolean success,
            String failureReason,
            List<ItemQuantity> items
    ) {
        this(UUID.randomUUID(), reservationId, playerId, reason, refId, success, failureReason, items, Instant.now());
    }

    public UUID sagaId() {
        return reservationId;
    }

    public UUID farmId() {
        return null;
    }

    public UUID plotId() {
        return refId;
    }

    public String seedItemCode() {
        return items != null && !items.isEmpty() ? items.get(0).itemCode() : null;
    }
}
