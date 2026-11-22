package ai_service.controller;

import idempotency.Idempotent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ai_service.dto.*;
import ai_service.service.NpcAgentService;
import ai_service.service.QuestGeneratorService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/npc")
@RequiredArgsConstructor
public class NpcController {

    private final NpcAgentService npcAgentService;
    private final QuestGeneratorService questGeneratorService;

    @PostMapping("/{npcCode}/talk")
    @Idempotent
    public ResponseEntity<NpcTalkResponse> talkToNpc(
            @AuthenticationPrincipal String principal,
            @PathVariable String npcCode,
            @Valid @RequestBody NpcTalkRequest request
    ) {
        UUID playerId = UUID.fromString(principal);
        return ResponseEntity.ok(npcAgentService.talkToNpc(playerId, npcCode, request));
    }

    @PostMapping("/{npcCode}/generate-quest")
    @Idempotent
    public ResponseEntity<PregeneratedQuestResponse> generateQuest(
            @AuthenticationPrincipal String principal,
            @PathVariable String npcCode,
            @RequestBody(required = false) GenerateQuestRequest request
    ) {
        UUID playerId = UUID.fromString(principal);
        GenerateQuestRequest req = request != null ? request : new GenerateQuestRequest(npcCode, "manual request");
        return ResponseEntity.ok(questGeneratorService.generatePersonalizedQuest(playerId, req));
    }

    @GetMapping("/ready-quests")
    public ResponseEntity<List<PregeneratedQuestResponse>> getReadyQuests(@AuthenticationPrincipal String principal) {
        UUID playerId = UUID.fromString(principal);
        return ResponseEntity.ok(questGeneratorService.getReadyQuests(playerId));
    }
}
