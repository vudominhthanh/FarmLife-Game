package world_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CraftQueueResponse(
        UUID queueId,
        UUID playerId,
        UUID machineObjectId,
        String recipeCode,
        Integer quantity,
        String status,
        OffsetDateTime startedAt,
        OffsetDateTime finishesAt,
        Long version,
        OffsetDateTime createdAt
) {}
