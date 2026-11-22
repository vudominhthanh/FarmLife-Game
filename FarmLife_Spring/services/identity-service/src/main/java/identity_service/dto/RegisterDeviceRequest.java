package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDeviceRequest(
        @NotBlank
        @Size(max = 100)
        String deviceId,

        @NotBlank
        @Pattern(regexp = "ios|android|web|pc", message = "Platform must be one of: ios, android, web, pc")
        String platform,

        String pushToken
) {}
