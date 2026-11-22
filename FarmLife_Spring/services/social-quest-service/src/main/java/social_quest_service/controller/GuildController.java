package social_quest_service.controller;

import idempotency.Idempotent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import social_quest_service.dto.GuildCreateRequest;
import social_quest_service.dto.GuildMemberResponse;
import social_quest_service.dto.GuildResponse;
import social_quest_service.service.GuildService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/guilds")
@RequiredArgsConstructor
public class GuildController {

    private final GuildService guildService;

    @PostMapping
    @Idempotent
    public ResponseEntity<GuildResponse> createGuild(
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody GuildCreateRequest request
    ) {
        UUID leaderId = UUID.fromString(principal);
        return ResponseEntity.ok(guildService.createGuild(leaderId, request));
    }

    @GetMapping("/me")
    public ResponseEntity<GuildResponse> getMyGuild(@AuthenticationPrincipal String principal) {
        UUID playerId = UUID.fromString(principal);
        return ResponseEntity.ok(guildService.getMyGuild(playerId));
    }

    @GetMapping("/{guildId}")
    public ResponseEntity<GuildResponse> getGuild(@PathVariable UUID guildId) {
        return ResponseEntity.ok(guildService.getGuild(guildId));
    }

    @PostMapping("/{guildId}/join")
    @Idempotent
    public ResponseEntity<GuildMemberResponse> joinGuild(
            @AuthenticationPrincipal String principal,
            @PathVariable UUID guildId
    ) {
        UUID playerId = UUID.fromString(principal);
        return ResponseEntity.ok(guildService.joinGuild(playerId, guildId));
    }

    @PostMapping("/{guildId}/leave")
    public ResponseEntity<Void> leaveGuild(
            @AuthenticationPrincipal String principal,
            @PathVariable UUID guildId
    ) {
        UUID playerId = UUID.fromString(principal);
        guildService.leaveGuild(playerId, guildId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{guildId}/members")
    public ResponseEntity<List<GuildMemberResponse>> getGuildMembers(@PathVariable UUID guildId) {
        return ResponseEntity.ok(guildService.getGuildMembers(guildId));
    }
}
