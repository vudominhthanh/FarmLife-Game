package world_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ActivePlantingResponse(
        UUID plotId,
        String cropCode,
        Short currentStage,
        OffsetDateTime plantedAt,
        OffsetDateTime nextStageAt,
        OffsetDateTime readyAt,
        boolean watered,
        Short harvestCount,
        boolean withered,
        String status,
        Long version
) {}
