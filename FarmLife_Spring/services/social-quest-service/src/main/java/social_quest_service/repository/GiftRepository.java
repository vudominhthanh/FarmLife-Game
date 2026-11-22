package social_quest_service.repository;

import social_quest_service.entity.Gift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GiftRepository extends JpaRepository<Gift, UUID> {
    List<Gift> findByReceiverIdAndClaimedFalse(UUID receiverId);
    List<Gift> findBySenderId(UUID senderId);
}
