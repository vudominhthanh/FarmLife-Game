package social_quest_service.service.impl;

import exception.BusinessException;
import exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social_quest_service.dto.ClaimQuestRewardResponse;
import social_quest_service.dto.PlayerQuestResponse;
import social_quest_service.dto.QuestResponse;
import social_quest_service.entity.PlayerQuest;
import social_quest_service.entity.Quest;
import social_quest_service.entity.enums.PlayerQuestStatus;
import social_quest_service.mapper.SocialQuestMapper;
import social_quest_service.repository.PlayerQuestRepository;
import social_quest_service.repository.QuestRepository;
import social_quest_service.service.QuestService;
import social_quest_service.service.SocialQuestOutboxService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestServiceImpl implements QuestService {

    private final QuestRepository questRepository;
    private final PlayerQuestRepository playerQuestRepository;
    private final SocialQuestOutboxService outboxService;

    @Override
    @Transactional(readOnly = true)
    public List<QuestResponse> getAvailableQuests() {
        return questRepository.findByIsActiveTrue().stream()
                .map(SocialQuestMapper::toQuestResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerQuestResponse> getMyQuests(UUID playerId) {
        return playerQuestRepository.findByPlayerId(playerId).stream()
                .map(SocialQuestMapper::toPlayerQuestResponse)
                .toList();
    }

    @Override
    @Transactional
    public void assignStarterQuests(UUID playerId) {
        List<Quest> starterQuests = questRepository.findByIsActiveTrue();
        for (Quest q : starterQuests) {
            boolean alreadyAssigned = playerQuestRepository
                    .findByPlayerIdAndQuestCodeAndStatus(playerId, q.getQuestCode(), PlayerQuestStatus.ACTIVE)
                    .isPresent();
            if (!alreadyAssigned) {
                PlayerQuest pq = PlayerQuest.builder()
                        .playerId(playerId)
                        .questCode(q.getQuestCode())
                        .progress(0)
                        .status(PlayerQuestStatus.ACTIVE)
                        .assignedAt(Instant.now())
                        .version(0L)
                        .build();
                playerQuestRepository.save(pq);
            }
        }
        log.info("Assigned starter quests to player {}", playerId);
    }

    @Override
    @Transactional
    public void assignQuest(UUID playerId, String questCode) {
        Quest quest = questRepository.findById(questCode)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Quest not found: " + questCode));

        boolean exists = playerQuestRepository
                .findByPlayerIdAndQuestCodeAndStatus(playerId, questCode, PlayerQuestStatus.ACTIVE)
                .isPresent();
        if (exists) {
            log.info("Player {} already has active quest {}", playerId, questCode);
            return;
        }

        PlayerQuest pq = PlayerQuest.builder()
                .playerId(playerId)
                .questCode(questCode)
                .progress(0)
                .status(PlayerQuestStatus.ACTIVE)
                .assignedAt(Instant.now())
                .version(0L)
                .build();
        playerQuestRepository.save(pq);
        log.info("Assigned quest {} to player {}", questCode, playerId);
    }

    @Override
    @Transactional
    public void progressQuests(UUID playerId, String goalType, int amount) {
        List<PlayerQuest> activeQuests = playerQuestRepository.findByPlayerIdAndStatus(playerId, PlayerQuestStatus.ACTIVE);
        for (PlayerQuest pq : activeQuests) {
            Quest quest = questRepository.findById(pq.getQuestCode()).orElse(null);
            if (quest != null && (quest.getGoalType().equalsIgnoreCase(goalType) || "ANY".equalsIgnoreCase(quest.getGoalType()))) {
                int newProgress = pq.getProgress() + amount;
                pq.setProgress(newProgress);
                if (newProgress >= quest.getGoalTarget()) {
                    pq.setStatus(PlayerQuestStatus.COMPLETED);
                    pq.setCompletedAt(Instant.now());
                    log.info("Player {} completed quest {}", playerId, pq.getQuestCode());
                }
                playerQuestRepository.save(pq);
            }
        }
    }

    @Override
    @Transactional
    public ClaimQuestRewardResponse claimReward(UUID playerId, UUID playerQuestId) {
        PlayerQuest pq = playerQuestRepository.findById(playerQuestId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Player quest not found"));

        if (!pq.getPlayerId().equals(playerId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "Quest does not belong to this player");
        }

        if (pq.getStatus() != PlayerQuestStatus.COMPLETED) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Quest is not in COMPLETED status: " + pq.getStatus());
        }

        Quest quest = questRepository.findById(pq.getQuestCode())
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Quest definition not found"));

        pq.setStatus(PlayerQuestStatus.CLAIMED);
        pq.setClaimedAt(Instant.now());
        playerQuestRepository.save(pq);

        Map<String, Object> payload = new HashMap<>();
        payload.put("playerId", playerId.toString());
        payload.put("playerQuestId", playerQuestId.toString());
        payload.put("questCode", pq.getQuestCode());
        payload.put("reward", quest.getRewardJson());

        outboxService.enqueue("PLAYER_QUEST", playerQuestId.toString(), "quest-completed.v1", payload);

        return new ClaimQuestRewardResponse(
                playerQuestId,
                pq.getQuestCode(),
                quest.getRewardJson(),
                true,
                "Reward claimed successfully"
        );
    }
}
