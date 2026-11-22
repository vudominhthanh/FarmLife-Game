package dto;

import java.time.Instant;
import java.util.UUID;

public record PlayerStatResponse(
        UUID playerId,
        Long totalHarvests,
        Long totalAnimalsRaised,
        Long totalCoinsEarned,
        Long totalTrades,
        Integer friendCount,
        Long playtimeMinutes,
        Instant lastActiveAt,
        Instant updatedAt
) {}
