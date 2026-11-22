package controller;

import dto.CustomizationResponse;
import dto.UpdateCustomizationRequest;
import service.CustomizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/players/{playerId}/customizations")
@RequiredArgsConstructor
public class CustomizationController {

    private final CustomizationService customizationService;

    @GetMapping
    public ResponseEntity<CustomizationResponse> getCustomization(@PathVariable UUID playerId) {
        return ResponseEntity.ok(customizationService.getCustomization(playerId));
    }

    @PutMapping
    public ResponseEntity<CustomizationResponse> updateCustomization(
            @PathVariable UUID playerId,
            @Valid @RequestBody UpdateCustomizationRequest request
    ) {
        return ResponseEntity.ok(customizationService.updateCustomization(playerId, request));
    }
}
