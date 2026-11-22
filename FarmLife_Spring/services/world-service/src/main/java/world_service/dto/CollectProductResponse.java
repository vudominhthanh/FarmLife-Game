package world_service.dto;

import java.util.UUID;

public record CollectProductResponse(
        UUID animalId,
        String productItemCode,
        int quantity,
        int expGained
) {}
