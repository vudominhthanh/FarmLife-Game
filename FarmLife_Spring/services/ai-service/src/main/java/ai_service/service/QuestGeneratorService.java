package ai_service.service;

import ai_service.dto.GenerateQuestRequest;
import ai_service.dto.PregeneratedQuestResponse;

import java.util.List;
import java.util.UUID;

public interface QuestGeneratorService {
    PregeneratedQuestResponse generatePersonalizedQuest(UUID playerId, GenerateQuestRequest request);
    List<PregeneratedQuestResponse> getReadyQuests(UUID playerId);
}
