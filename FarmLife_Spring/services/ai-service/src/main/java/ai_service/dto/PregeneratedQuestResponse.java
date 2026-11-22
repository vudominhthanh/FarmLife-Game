package ai_service.dto;

import ai_service.entity.enums.PregenStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PregeneratedQuestResponse(
        UUID pregenId,
        UUID playerId,
        String npcCode,
        Integer ruleId,
        Map<String, Object> questPayload,
        String dialogueText,
        PregenStatus status,
        Instant expiresAt
) {}
