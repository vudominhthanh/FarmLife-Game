package social_quest_service.repository;

import social_quest_service.entity.PlayerQuest;
import social_quest_service.entity.enums.PlayerQuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerQuestRepository extends JpaRepository<PlayerQuest, UUID> {
    List<PlayerQuest> findByPlayerId(UUID playerId);
    List<PlayerQuest> findByPlayerIdAndStatus(UUID playerId, PlayerQuestStatus status);
    Optional<PlayerQuest> findByPlayerIdAndQuestCodeAndStatus(UUID playerId, String questCode, PlayerQuestStatus status);
    List<PlayerQuest> findByPlayerIdAndStatusIn(UUID playerId, List<PlayerQuestStatus> statuses);
}
