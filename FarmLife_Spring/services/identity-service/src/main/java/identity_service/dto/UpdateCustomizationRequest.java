package dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record UpdateCustomizationRequest(
        @Size(max = 50)
        String avatarCode,

        @Size(max = 300)
        String customAvatarAssetKey,

        @Size(max = 50)
        String frameCode,

        @Size(max = 50)
        String farmThemeCode,

        @Size(max = 20)
        String uiPalette,

        @Size(max = 50)
        String petCompanionCode
) {
    @AssertTrue(message = "avatarCode and customAvatarAssetKey cannot both be set")
    public boolean isAvatarConfigurationValid() {
        return avatarCode == null || customAvatarAssetKey == null;
    }
}
