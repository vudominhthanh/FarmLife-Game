package social_quest_service.dto;

import java.util.Map;

public record AchievementResponse(
        String achievementCode,
        String title,
        Map<String, Object> criteriaJson,
        Map<String, Object> rewardJson,
        Boolean isActive
) {}
