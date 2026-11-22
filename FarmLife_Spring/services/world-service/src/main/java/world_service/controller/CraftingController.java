package world_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import world_service.dto.CraftQueueResponse;
import world_service.dto.CraftRequest;
import world_service.service.CraftingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farms/{farmId}/crafts")
@RequiredArgsConstructor
public class CraftingController {

    private final CraftingService craftingService;

    @GetMapping
    public ResponseEntity<List<CraftQueueResponse>> getQueue(
            @PathVariable UUID farmId,
            @RequestHeader("X-Player-Id") UUID playerId
    ) {
        return ResponseEntity.ok(craftingService.getQueue(farmId, playerId));
    }

    @PostMapping
    public ResponseEntity<CraftQueueResponse> startCraft(
            @PathVariable UUID farmId,
            @RequestHeader("X-Player-Id") UUID playerId,
            @Valid @RequestBody CraftRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(craftingService.startCraft(farmId, playerId, request));
    }
}
