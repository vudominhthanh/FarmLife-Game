package repository;

import entity.MailboxItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MailboxItemRepository extends JpaRepository<MailboxItem, UUID> {
    List<MailboxItem> findAllByPlayerIdAndClaimedAtIsNull(UUID playerId);
    Optional<MailboxItem> findByMailItemIdAndPlayerId(UUID mailItemId, UUID playerId);
}
