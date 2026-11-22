package dto;

import tools.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record PlayerSettingsResponse(
        UUID playerId,
        String language,
        Short musicVolume,
        Short sfxVolume,
        JsonNode notifications,
        JsonNode privacy,
        Long version,
        Instant updatedAt
) {}
