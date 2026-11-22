package contracts;

import java.util.UUID;

public record InventorySlotDelta(
        UUID slotId,
        short slotIndex,
        String itemCode,
        int delta,
        int quantity,
        int lockedQuantity,
        short quality
) {
    public InventorySlotDelta(
            UUID slotId,
            short slotIndex,
            String itemCode,
            int delta,
            int remainingQuantity
    ) {
        this(slotId, slotIndex, itemCode, delta, remainingQuantity, 0, (short) 100);
    }

    public int remainingQuantity() {
        return quantity;
    }
}
