package service;

import dto.ClaimMailboxResponse;
import dto.MailboxItemResponse;
import entity.MailboxItem;
import exception.BusinessException;
import exception.CommonErrorCode;
import mapper.EconomyMapper;
import repository.MailboxItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailboxServiceImpl implements MailboxService {

    private final MailboxItemRepository mailboxRepository;
    private final EconomyInventoryService inventoryService;
    private final EconomyMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<MailboxItemResponse> getMailbox(UUID playerId) {
        return mailboxRepository.findAllByPlayerIdAndClaimedAtIsNull(playerId)
                .stream()
                .map(mapper::toMailboxResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClaimMailboxResponse claimMailboxItem(UUID playerId, UUID mailId) {
        MailboxItem item = mailboxRepository.findByMailItemIdAndPlayerId(mailId, playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Mailbox item not found: " + mailId));

        if (item.getClaimedAt() != null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Item already claimed");
        }

        item.setClaimedAt(Instant.now());
        mailboxRepository.save(item);

        inventoryService.addItem(playerId, item.getItemCode(), item.getQuantity(), item.getQuality(), item.getSourceType(), mailId);
        log.info("Player {} claimed mailbox item {} ({} x {})", playerId, mailId, item.getQuantity(), item.getItemCode());
        return new ClaimMailboxResponse(mailId, item.getItemCode(), item.getQuantity(), true, "Item claimed to inventory");
    }

    @Override
    @Transactional
    public List<ClaimMailboxResponse> claimAll(UUID playerId) {
        List<MailboxItem> unclaimed = mailboxRepository.findAllByPlayerIdAndClaimedAtIsNull(playerId);
        List<ClaimMailboxResponse> responses = new ArrayList<>();
        Instant now = Instant.now();

        for (MailboxItem item : unclaimed) {
            item.setClaimedAt(now);
            mailboxRepository.save(item);
            inventoryService.addItem(playerId, item.getItemCode(), item.getQuantity(), item.getQuality(), item.getSourceType(), item.getMailItemId());
            responses.add(new ClaimMailboxResponse(item.getMailItemId(), item.getItemCode(), item.getQuantity(), true, "Item claimed"));
        }

        log.info("Player {} claimed all {} mailbox items", playerId, responses.size());
        return responses;
    }
}
