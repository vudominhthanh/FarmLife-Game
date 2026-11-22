package ai_service.controller;

import idempotency.Idempotent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ai_service.dto.AskGuideRequest;
import ai_service.dto.AskGuideResponse;
import ai_service.service.RagKnowledgeService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/guide")
@RequiredArgsConstructor
public class GuideController {

    private final RagKnowledgeService ragKnowledgeService;

    @PostMapping("/ask")
    @Idempotent
    public ResponseEntity<AskGuideResponse> askGuide(
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody AskGuideRequest request
    ) {
        UUID playerId = UUID.fromString(principal);
        return ResponseEntity.ok(ragKnowledgeService.askGuide(playerId, request));
    }
}
