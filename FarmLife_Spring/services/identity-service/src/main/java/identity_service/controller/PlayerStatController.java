package controller;

import dto.PlayerStatResponse;
import service.PlayerStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/players/{playerId}/stats")
@RequiredArgsConstructor
public class PlayerStatController {

    private final PlayerStatService statService;

    @GetMapping
    public ResponseEntity<PlayerStatResponse> getStats(@PathVariable UUID playerId) {
        return ResponseEntity.ok(statService.getStats(playerId));
    }
}
