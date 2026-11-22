package world_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import world_service.dto.AnimalResponse;
import world_service.dto.CollectProductResponse;
import world_service.service.AnimalService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farms/{farmId}/animals")
@RequiredArgsConstructor
public class AnimalController {

    private final AnimalService animalService;

    @GetMapping
    public ResponseEntity<List<AnimalResponse>> getAnimals(
            @PathVariable UUID farmId,
            @RequestHeader("X-Player-Id") UUID playerId
    ) {
        return ResponseEntity.ok(animalService.getAnimals(farmId, playerId));
    }

    @PostMapping("/{animalId}/feed")
    public ResponseEntity<AnimalResponse> feedAnimal(
            @PathVariable UUID farmId,
            @PathVariable UUID animalId,
            @RequestHeader("X-Player-Id") UUID playerId
    ) {
        return ResponseEntity.ok(animalService.feedAnimal(farmId, animalId, playerId));
    }

    @PostMapping("/{animalId}/collect")
    public ResponseEntity<CollectProductResponse> collectProduct(
            @PathVariable UUID farmId,
            @PathVariable UUID animalId,
            @RequestHeader("X-Player-Id") UUID playerId
    ) {
        return ResponseEntity.ok(animalService.collectProduct(farmId, animalId, playerId));
    }
}
