package world_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import world_service.dto.SyncReplayRequest;
import world_service.dto.SyncReplayResponse;
import world_service.service.ActionReplayService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farms/actions")
@RequiredArgsConstructor
public class ActionReplayController {

    private final ActionReplayService actionReplayService;

    @PostMapping("/sync")
    public ResponseEntity<SyncReplayResponse> syncActions(
            @RequestHeader("X-Player-Id") UUID playerId,
            @Valid @RequestBody SyncReplayRequest request
    ) {
        return ResponseEntity.ok(actionReplayService.processReplay(playerId, request));
    }
}
