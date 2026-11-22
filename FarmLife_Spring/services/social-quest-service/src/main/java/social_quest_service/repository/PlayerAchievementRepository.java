package social_quest_service.repository;

import social_quest_service.entity.PlayerAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayerAchievementRepository extends JpaRepository<PlayerAchievement, PlayerAchievement.PlayerAchievementId> {
    List<PlayerAchievement> findByPlayerId(UUID playerId);
}
