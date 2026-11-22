package ai_service.service;

import ai_service.dto.NpcTalkRequest;
import ai_service.dto.NpcTalkResponse;

import java.util.UUID;

public interface NpcAgentService {
    NpcTalkResponse talkToNpc(UUID playerId, String npcCode, NpcTalkRequest request);
}
