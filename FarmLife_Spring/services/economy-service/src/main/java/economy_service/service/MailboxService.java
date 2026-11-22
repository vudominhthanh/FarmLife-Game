package service;

import dto.ClaimMailboxResponse;
import dto.MailboxItemResponse;

import java.util.List;
import java.util.UUID;

public interface MailboxService {
    List<MailboxItemResponse> getMailbox(UUID playerId);
    ClaimMailboxResponse claimMailboxItem(UUID playerId, UUID mailId);
    List<ClaimMailboxResponse> claimAll(UUID playerId);
}
