package world_service.service;

import contracts.ReservationResolvedEvent;
import world_service.dto.ActivePlantingResponse;
import world_service.dto.HarvestResponse;
import world_service.dto.PlantCropRequest;
import world_service.dto.PlotResponse;

import java.util.UUID;

public interface PlantingService {
    ActivePlantingResponse plantCrop(UUID farmId, UUID plotId, UUID playerId, PlantCropRequest request);
    PlotResponse waterPlot(UUID farmId, UUID plotId, UUID playerId);
    HarvestResponse harvestCrop(UUID farmId, UUID plotId, UUID playerId);
    void handleReservationResolved(ReservationResolvedEvent event);
}
