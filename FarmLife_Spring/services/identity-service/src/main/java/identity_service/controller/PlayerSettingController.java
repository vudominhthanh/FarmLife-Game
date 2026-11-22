package controller;

import dto.PlayerSettingsResponse;
import dto.UpdateSettingsRequest;
import service.PlayerSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/players/{playerId}/settings")
@RequiredArgsConstructor
public class PlayerSettingController {

    private final PlayerSettingService settingService;

    @GetMapping
    public ResponseEntity<PlayerSettingsResponse> getSettings(@PathVariable UUID playerId) {
        return ResponseEntity.ok(settingService.getSettings(playerId));
    }

    @PutMapping
    public ResponseEntity<PlayerSettingsResponse> updateSettings(
            @PathVariable UUID playerId,
            @Valid @RequestBody UpdateSettingsRequest request
    ) {
        return ResponseEntity.ok(settingService.updateSettings(playerId, request));
    }
}
