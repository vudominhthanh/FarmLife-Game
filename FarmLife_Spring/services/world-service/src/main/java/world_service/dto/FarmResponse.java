package world_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FarmResponse(
        UUID farmId,
        UUID playerId,
        String name,
        Integer sizeLevel,
        Short gridWidth,
        Short gridHeight,
        String weather,
        String season,
        Long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
