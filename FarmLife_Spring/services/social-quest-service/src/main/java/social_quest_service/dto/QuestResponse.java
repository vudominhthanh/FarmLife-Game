package social_quest_service.dto;

import social_quest_service.entity.enums.QuestSource;
import social_quest_service.entity.enums.QuestType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record QuestResponse(
        String questCode,
        String title,
        String description,
        QuestType type,
        String goalType,
        Integer goalTarget,
        Map<String, Object> rewardJson,
        Instant startsAt,
        Instant endsAt,
        Boolean isPersonalized,
        QuestSource source,
        String sourceNpcCode,
        UUID pregenId,
        Boolean isActive
) {}
