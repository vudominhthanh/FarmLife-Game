package world_service.dto;

import java.util.UUID;

public record HarvestResponse(
        UUID plotId,
        String harvestItemCode,
        int quantity,
        short quality,
        int expGained
) {}
