package social_quest_service.dto;

import java.time.Instant;
import java.util.UUID;

public record PlayerAchievementResponse(
        UUID playerId,
        String achievementCode,
        Instant unlockedAt
) {}
