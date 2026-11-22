package social_quest_service.controller;

import idempotency.Idempotent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import social_quest_service.dto.ClaimQuestRewardResponse;
import social_quest_service.dto.PlayerQuestResponse;
import social_quest_service.dto.QuestResponse;
import social_quest_service.service.QuestService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;

    @GetMapping
    public ResponseEntity<List<QuestResponse>> getAvailableQuests() {
        return ResponseEntity.ok(questService.getAvailableQuests());
    }

    @GetMapping("/me")
    public ResponseEntity<List<PlayerQuestResponse>> getMyQuests(@AuthenticationPrincipal String principal) {
        UUID playerId = UUID.fromString(principal);
        return ResponseEntity.ok(questService.getMyQuests(playerId));
    }

    @PostMapping("/{playerQuestId}/claim")
    @Idempotent
    public ResponseEntity<ClaimQuestRewardResponse> claimReward(
            @AuthenticationPrincipal String principal,
            @PathVariable UUID playerQuestId
    ) {
        UUID playerId = UUID.fromString(principal);
        return ResponseEntity.ok(questService.claimReward(playerId, playerQuestId));
    }
}
