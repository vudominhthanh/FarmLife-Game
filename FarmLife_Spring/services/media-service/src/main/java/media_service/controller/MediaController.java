package media_service.controller;

import idempotency.Idempotent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import media_service.dto.*;
import media_service.service.MediaUploadService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaUploadService mediaUploadService;

    @PostMapping("/upload-intent")
    @Idempotent
    public ResponseEntity<UploadIntentResponse> createUploadIntent(
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody UploadIntentRequest request
    ) {
        UUID playerId = UUID.fromString(principal);
        return ResponseEntity.ok(mediaUploadService.createUploadIntent(playerId, request));
    }

    @PostMapping("/upload-intent/{intentId}/validate")
    @Idempotent
    public ResponseEntity<AssetResponse> validateAndPromote(
            @AuthenticationPrincipal String principal,
            @PathVariable UUID intentId
    ) {
        UUID playerId = UUID.fromString(principal);
        return ResponseEntity.ok(mediaUploadService.validateAndPromote(playerId, intentId));
    }

    @GetMapping("/assets/{assetKey}")
    public ResponseEntity<AssetResponse> getAsset(@PathVariable String assetKey) {
        return ResponseEntity.ok(mediaUploadService.getAsset(assetKey));
    }

    @GetMapping("/assets/{assetKey}/download-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedDownloadUrl(@PathVariable String assetKey) {
        return ResponseEntity.ok(mediaUploadService.getPresignedDownloadUrl(assetKey));
    }
}
