package world_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AnimalResponse(
        UUID animalId,
        UUID playerId,
        UUID housingObjectId,
        String animalTypeCode,
        String name,
        Long ageSec,
        Short hunger,
        Short happiness,
        String stage,
        OffsetDateTime lastFedAt,
        OffsetDateTime lastProductAt,
        Long version,
        OffsetDateTime createdAt
) {}
