package contracts;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ItemReserveRequestedEvent(
        UUID eventId,
        UUID reservationId,
        UUID playerId,
        String reason,
        UUID refId,
        List<ItemQuantity> items,
        Instant requestedAt
) {
    // Constructor fallback for simple single crop planting request
    public ItemReserveRequestedEvent(
            UUID eventId,
            UUID sagaId,
            UUID playerId,
            UUID farmId,
            UUID plotId,
            String seedItemCode,
            int quantity,
            Instant requestedAt
    ) {
        this(
                eventId,
                sagaId,
                playerId,
                "planting",
                plotId,
                List.of(new ItemQuantity(seedItemCode, quantity)),
                requestedAt
        );
    }
}
