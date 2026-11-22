package social_quest_service.service;

import social_quest_service.dto.ClaimQuestRewardResponse;
import social_quest_service.dto.PlayerQuestResponse;
import social_quest_service.dto.QuestResponse;

import java.util.List;
import java.util.UUID;

public interface QuestService {
    List<QuestResponse> getAvailableQuests();
    List<PlayerQuestResponse> getMyQuests(UUID playerId);
    void assignStarterQuests(UUID playerId);
    void assignQuest(UUID playerId, String questCode);
    void progressQuests(UUID playerId, String goalType, int amount);
    ClaimQuestRewardResponse claimReward(UUID playerId, UUID playerQuestId);
}
