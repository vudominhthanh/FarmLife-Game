package controller;

import dto.ClaimMailboxResponse;
import dto.MailboxItemResponse;
import service.MailboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mailbox")
@RequiredArgsConstructor
public class MailboxController {

    private final MailboxService mailboxService;

    @GetMapping
    public ResponseEntity<List<MailboxItemResponse>> getMailbox(@RequestHeader("X-Player-Id") UUID playerId) {
        return ResponseEntity.ok(mailboxService.getMailbox(playerId));
    }

    @PostMapping("/{mailId}/claim")
    public ResponseEntity<ClaimMailboxResponse> claimItem(
            @RequestHeader("X-Player-Id") UUID playerId,
            @PathVariable UUID mailId
    ) {
        return ResponseEntity.ok(mailboxService.claimMailboxItem(playerId, mailId));
    }

    @PostMapping("/claim-all")
    public ResponseEntity<List<ClaimMailboxResponse>> claimAll(@RequestHeader("X-Player-Id") UUID playerId) {
        return ResponseEntity.ok(mailboxService.claimAll(playerId));
    }
}
