package dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

public record UpdateSettingsRequest(
        @Size(max = 10)
        @Pattern(regexp = "^[a-zA-Z-]+$")
        String language,

        @Min(0)
        @Max(100)
        Short musicVolume,

        @Min(0)
        @Max(100)
        Short sfxVolume,

        JsonNode notifications,
        JsonNode privacy
) {}
