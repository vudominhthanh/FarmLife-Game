package controller;

import dto.PlaystyleResponse;
import dto.UpdatePlaystyleRequest;
import service.PlaystyleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/players/{playerId}/playstyle")
@RequiredArgsConstructor
public class PlaystyleController {

    private final PlaystyleService playstyleService;

    @GetMapping
    public ResponseEntity<PlaystyleResponse> getPlaystyle(@PathVariable UUID playerId) {
        return ResponseEntity.ok(playstyleService.getPlaystyle(playerId));
    }

    @PutMapping
    public ResponseEntity<PlaystyleResponse> updatePlaystyle(
            @PathVariable UUID playerId,
            @Valid @RequestBody UpdatePlaystyleRequest request
    ) {
        return ResponseEntity.ok(playstyleService.updatePlaystyle(playerId, request));
    }
}
