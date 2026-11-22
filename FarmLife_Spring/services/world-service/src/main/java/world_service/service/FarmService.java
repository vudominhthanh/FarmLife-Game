package world_service.service;

import world_service.dto.FarmResponse;
import world_service.dto.PlotResponse;
import world_service.entity.Farm;

import java.util.List;
import java.util.UUID;

public interface FarmService {
    FarmResponse getFarmByPlayer(UUID playerId);
    FarmResponse getFarm(UUID farmId);
    Farm createInitialFarmForPlayer(UUID playerId, String farmName);
    List<PlotResponse> getPlots(UUID farmId);
}
