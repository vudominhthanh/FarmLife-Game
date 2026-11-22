package social_quest_service.dto;

import social_quest_service.entity.enums.PlayerQuestStatus;

import java.time.Instant;
import java.util.UUID;

public record PlayerQuestResponse(
        UUID playerQuestId,
        UUID playerId,
        String questCode,
        Integer progress,
        PlayerQuestStatus status,
        Instant assignedAt,
        Instant completedAt,
        Instant claimedAt,
        Long version
) {}
