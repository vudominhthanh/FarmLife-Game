package controller;

import dto.PlayerProfileResponse;
import dto.UpdateProfileRequest;
import service.PlayerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/players/{playerId}/profile")
@RequiredArgsConstructor
public class PlayerProfileController {

    private final PlayerProfileService profileService;

    @GetMapping
    public ResponseEntity<PlayerProfileResponse> getProfile(@PathVariable UUID playerId) {
        return ResponseEntity.ok(profileService.getProfile(playerId));
    }

    @PutMapping
    public ResponseEntity<PlayerProfileResponse> updateProfile(
            @PathVariable UUID playerId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(profileService.updateProfile(playerId, request));
    }

    @PostMapping("/xp")
    public ResponseEntity<PlayerProfileResponse> addXp(
            @PathVariable UUID playerId,
            @RequestParam long xp
    ) {
        return ResponseEntity.ok(profileService.addXp(playerId, xp));
    }
}
