package world_service.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record PlacedObjectResponse(
        UUID objectId,
        UUID farmId,
        String objectCode,
        String objectType,
        Integer originX,
        Integer originY,
        Short width,
        Short height,
        Short direction,
        Integer maxCapacity,
        Map<String, Object> stateMetadata,
        Long version,
        OffsetDateTime placedAt
) {}
