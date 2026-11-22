package world_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import world_service.dto.FarmResponse;
import world_service.dto.PlotResponse;
import world_service.service.FarmService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;

    @GetMapping("/me")
    public ResponseEntity<FarmResponse> getMyFarm(@RequestHeader("X-Player-Id") UUID playerId) {
        return ResponseEntity.ok(farmService.getFarmByPlayer(playerId));
    }

    @GetMapping("/{farmId}")
    public ResponseEntity<FarmResponse> getFarm(@PathVariable UUID farmId) {
        return ResponseEntity.ok(farmService.getFarm(farmId));
    }

    @GetMapping("/{farmId}/plots")
    public ResponseEntity<List<PlotResponse>> getPlots(@PathVariable UUID farmId) {
        return ResponseEntity.ok(farmService.getPlots(farmId));
    }
}
