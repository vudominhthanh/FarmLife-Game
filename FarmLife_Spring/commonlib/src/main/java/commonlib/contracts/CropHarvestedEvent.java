package contracts;

import java.time.Instant;
import java.util.UUID;

public record CropHarvestedEvent(
        UUID eventId,
        UUID playerId,
        UUID farmId,
        UUID plotId,
        String harvestItemCode,
        int quantity,
        short quality,
        int expGained,
        Instant harvestedAt
) {
    public CropHarvestedEvent(
            UUID eventId,
            UUID playerId,
            UUID farmId,
            UUID plotId,
            String cropItemCode,
            int harvestedQuantity,
            int expGained,
            Instant harvestedAt
    ) {
        this(eventId, playerId, farmId, plotId, cropItemCode, harvestedQuantity, (short) 100, expGained, harvestedAt);
    }

    public String cropItemCode() {
        return harvestItemCode;
    }

    public int harvestedQuantity() {
        return quantity;
    }
}
