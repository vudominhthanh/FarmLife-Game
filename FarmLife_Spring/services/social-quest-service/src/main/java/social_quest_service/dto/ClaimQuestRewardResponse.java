package social_quest_service.dto;

import java.util.Map;
import java.util.UUID;

public record ClaimQuestRewardResponse(
        UUID playerQuestId,
        String questCode,
        Map<String, Object> rewardClaimed,
        boolean success,
        String message
) {}
