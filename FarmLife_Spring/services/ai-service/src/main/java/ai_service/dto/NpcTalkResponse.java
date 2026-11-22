package ai_service.dto;

import ai_service.entity.enums.FsmState;
import ai_service.entity.enums.Relationship;

public record NpcTalkResponse(
        String npcCode,
        String npcName,
        String reply,
        FsmState fsmState,
        Integer affectionScore,
        Relationship relationship
) {}
