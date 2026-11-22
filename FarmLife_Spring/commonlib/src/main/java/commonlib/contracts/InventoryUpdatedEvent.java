package contracts;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record InventoryUpdatedEvent(
        UUID playerId,
        UUID inventoryId,
        String reason,
        UUID refId,
        long coins,
        long gems,
        List<InventorySlotDelta> deltas,
        List<SlotSnapshot> slots,
        boolean overflowToMailbox,
        OffsetDateTime updatedAt
) {
    public InventoryUpdatedEvent(
            UUID playerId,
            UUID inventoryId,
            String reason,
            UUID refId,
            List<InventorySlotDelta> deltas,
            boolean overflowToMailbox
    ) {
        this(playerId, inventoryId, reason, refId, 0L, 0L, deltas, List.of(), overflowToMailbox, OffsetDateTime.now());
    }

    public InventoryUpdatedEvent(
            UUID playerId,
            String reason,
            long coins,
            long gems,
            List<SlotSnapshot> slots,
            boolean overflowToMailbox,
            OffsetDateTime updatedAt
    ) {
        this(playerId, null, reason, null, coins, gems, List.of(), slots, overflowToMailbox, updatedAt);
    }

    public record SlotSnapshot(
            UUID slotId,
            short slotIndex,
            String itemCode,
            int quantity,
            int lockedQuantity,
            short quality
    ) {}
}
