package world_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import world_service.dto.ActivePlantingResponse;
import world_service.dto.HarvestResponse;
import world_service.dto.PlantCropRequest;
import world_service.dto.PlotResponse;
import world_service.service.PlantingService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farms/{farmId}/plots/{plotId}")
@RequiredArgsConstructor
public class PlotController {

    private final PlantingService plantingService;

    @PostMapping("/plant")
    public ResponseEntity<ActivePlantingResponse> plantCrop(
            @PathVariable UUID farmId,
            @PathVariable UUID plotId,
            @RequestHeader("X-Player-Id") UUID playerId,
            @Valid @RequestBody PlantCropRequest request
    ) {
        return ResponseEntity.ok(plantingService.plantCrop(farmId, plotId, playerId, request));
    }

    @PostMapping("/water")
    public ResponseEntity<PlotResponse> waterPlot(
            @PathVariable UUID farmId,
            @PathVariable UUID plotId,
            @RequestHeader("X-Player-Id") UUID playerId
    ) {
        return ResponseEntity.ok(plantingService.waterPlot(farmId, plotId, playerId));
    }

    @PostMapping("/harvest")
    public ResponseEntity<HarvestResponse> harvestCrop(
            @PathVariable UUID farmId,
            @PathVariable UUID plotId,
            @RequestHeader("X-Player-Id") UUID playerId
    ) {
        return ResponseEntity.ok(plantingService.harvestCrop(farmId, plotId, playerId));
    }
}
