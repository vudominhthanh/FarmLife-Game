package dto;

import java.time.Instant;
import java.util.UUID;

public record CustomizationResponse(
        UUID playerId,
        String avatarCode,
        String customAvatarAssetKey,
        String frameCode,
        String farmThemeCode,
        String uiPalette,
        String petCompanionCode,
        Long version,
        Instant updatedAt
) {}
