package dto;

import contracts.InventorySlotDelta;

import java.util.List;

public record ReserveOutcome(
        boolean success,
        String failureReason,
        List<InventorySlotDelta> deltas
) {
    public static ReserveOutcome ok(List<InventorySlotDelta> deltas) {
        return new ReserveOutcome(true, null, deltas);
    }

    public static ReserveOutcome failed(String reason) {
        return new ReserveOutcome(false, reason, List.of());
    }
}
