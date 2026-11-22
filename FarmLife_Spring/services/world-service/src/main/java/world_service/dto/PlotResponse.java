package world_service.dto;

import java.util.UUID;

public record PlotResponse(
        UUID plotId,
        UUID farmId,
        Short coordX,
        Short coordY,
        Short soilQuality,
        String terrainType,
        boolean locked,
        ActivePlantingResponse activePlanting
) {}
