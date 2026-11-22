package dto;

import entity.enums.ItemCategory;
import entity.enums.ItemRarity;

import java.util.List;
import java.util.UUID;

public record InventoryResponse(
        UUID inventoryId,
        UUID playerId,
        int capacity,
        long unclaimedMailboxCount,
        List<SlotView> slots
) {
    public record SlotView(
            UUID slotId,
            short slotIndex,
            String itemCode,
            String itemName,
            ItemCategory category,
            ItemRarity rarity,
            String iconAssetKey,
            int quantity,
            int lockedQuantity,
            int maxStack,
            short quality
    ) {}
}

