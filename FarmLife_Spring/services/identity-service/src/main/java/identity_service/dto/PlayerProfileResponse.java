package dto;

import java.time.Instant;
import java.util.UUID;

public record PlayerProfileResponse(
        UUID playerId,
        String displayName,
        Integer level,
        Long xp,
        String titleCode,
        String bio,
        Long version,
        Instant updatedAt
) {}
