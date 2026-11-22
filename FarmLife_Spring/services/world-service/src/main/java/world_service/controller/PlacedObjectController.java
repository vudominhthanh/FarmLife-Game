package world_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import world_service.dto.PlacedObjectRequest;
import world_service.dto.PlacedObjectResponse;
import world_service.service.PlacedObjectService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farms/{farmId}/objects")
@RequiredArgsConstructor
public class PlacedObjectController {

    private final PlacedObjectService placedObjectService;

    @GetMapping
    public ResponseEntity<List<PlacedObjectResponse>> getObjects(@PathVariable UUID farmId) {
        return ResponseEntity.ok(placedObjectService.getObjects(farmId));
    }

    @PostMapping
    public ResponseEntity<PlacedObjectResponse> placeObject(
            @PathVariable UUID farmId,
            @RequestHeader("X-Player-Id") UUID playerId,
            @Valid @RequestBody PlacedObjectRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(placedObjectService.placeObject(farmId, playerId, request));
    }

    @DeleteMapping("/{objectId}")
    public ResponseEntity<Void> removeObject(
            @PathVariable UUID farmId,
            @PathVariable UUID objectId,
            @RequestHeader("X-Player-Id") UUID playerId
    ) {
        placedObjectService.removeObject(farmId, objectId, playerId);
        return ResponseEntity.noContent().build();
    }
}
