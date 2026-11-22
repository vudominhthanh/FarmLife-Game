package ai_service.service.impl;

import exception.BusinessException;
import exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import ai_service.dto.GenerateQuestRequest;
import ai_service.dto.PregeneratedQuestResponse;
import ai_service.entity.PregeneratedQuest;
import ai_service.entity.QuestGenerationRule;
import ai_service.entity.enums.PregenStatus;
import ai_service.repository.PregeneratedQuestRepository;
import ai_service.repository.QuestGenerationRuleRepository;
import ai_service.service.AiOutboxService;
import ai_service.service.QuestGeneratorService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestGeneratorServiceImpl implements QuestGeneratorService {

    private final QuestGenerationRuleRepository ruleRepository;
    private final PregeneratedQuestRepository pregeneratedQuestRepository;
    private final AiOutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PregeneratedQuestResponse generatePersonalizedQuest(UUID playerId, GenerateQuestRequest request) {
        // Retrieve active rules
        List<QuestGenerationRule> rules = ruleRepository.findByIsActiveTrueOrderByPriorityAsc();
        if (rules.isEmpty()) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "No active quest generation rules configured");
        }
        QuestGenerationRule rule = rules.getFirst();

        // 1. Propose target and rewards (simulating LLM proposal or templates)
        int requestedTarget = 15;
        long requestedCoins = 500L;
        long requestedXp = 250L;
        String goalType = rule.getAllowedGoals().getFirst();
        String itemCode = rule.getAllowedItems().getFirst();

        // 2. Rule #10: Guardrail Clamping with Math.clamp
        int clampedTarget = Math.clamp(requestedTarget, rule.getMinTargetQty(), rule.getMaxTargetQty());
        long clampedCoins = Math.min(requestedCoins, rule.getMaxCoinReward());
        long clampedXp = Math.min(requestedXp, rule.getMaxXpReward());

        Map<String, Object> rewardMap = new HashMap<>();
        rewardMap.put("coins", clampedCoins);
        rewardMap.put("xp", clampedXp);

        String questCode = "AI_QUEST_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Map<String, Object> questPayload = new HashMap<>();
        questPayload.put("questCode", questCode);
        questPayload.put("title", "Thu hoạch " + itemCode + " cho " + request.npcCode());
        questPayload.put("goalType", goalType);
        questPayload.put("goalTarget", clampedTarget);
        questPayload.put("itemCode", itemCode);
        questPayload.put("reward", rewardMap);

        String dialogueText = String.format("Chào bác nông dân! Tôi đang rất cần %d %s. Bạn có thể giúp tôi thu hoạch không?",
                clampedTarget, itemCode);

        PregeneratedQuest pregen = PregeneratedQuest.builder()
                .playerId(playerId)
                .npcCode(request.npcCode())
                .ruleId(rule.getRuleId())
                .questPayload(questPayload)
                .dialogueText(dialogueText)
                .status(PregenStatus.READY)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .createdAt(Instant.now())
                .build();

        pregen = pregeneratedQuestRepository.save(pregen);

        // 3. Publish quest-assigned.v1 Outbox Event
        try {
            Map<String, Object> eventPayload = new HashMap<>();
            eventPayload.put("eventId", UUID.randomUUID().toString());
            eventPayload.put("playerId", playerId.toString());
            eventPayload.put("questCode", questCode);
            eventPayload.put("pregenId", pregen.getPregenId().toString());
            eventPayload.put("sourceNpcCode", request.npcCode());
            eventPayload.put("title", (String) questPayload.get("title"));
            eventPayload.put("goalType", goalType);
            eventPayload.put("goalTarget", clampedTarget);
            eventPayload.put("rewardJson", objectMapper.writeValueAsString(rewardMap));
            eventPayload.put("assignedAt", Instant.now().toString());

            outboxService.enqueue("PREGEN_QUEST", pregen.getPregenId().toString(), "quest-assigned.v1", eventPayload);
        } catch (Exception ex) {
            log.error("Failed to serialize rewardJson for quest-assigned outbox event", ex);
        }

        log.info("Generated guardrail-clamped quest {} for player {} via NPC {}",
                questCode, playerId, request.npcCode());

        return new PregeneratedQuestResponse(
                pregen.getPregenId(),
                playerId,
                pregen.getNpcCode(),
                pregen.getRuleId(),
                pregen.getQuestPayload(),
                pregen.getDialogueText(),
                pregen.getStatus(),
                pregen.getExpiresAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PregeneratedQuestResponse> getReadyQuests(UUID playerId) {
        return pregeneratedQuestRepository.findByPlayerIdAndStatus(playerId, PregenStatus.READY).stream()
                .map(p -> new PregeneratedQuestResponse(
                        p.getPregenId(),
                        p.getPlayerId(),
                        p.getNpcCode(),
                        p.getRuleId(),
                        p.getQuestPayload(),
                        p.getDialogueText(),
                        p.getStatus(),
                        p.getExpiresAt()
                ))
                .toList();
    }
}
