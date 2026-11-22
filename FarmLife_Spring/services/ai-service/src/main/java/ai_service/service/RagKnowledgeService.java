package ai_service.service;

import ai_service.dto.AskGuideRequest;
import ai_service.dto.AskGuideResponse;

import java.util.UUID;

public interface RagKnowledgeService {
    AskGuideResponse askGuide(UUID playerId, AskGuideRequest request);
}
