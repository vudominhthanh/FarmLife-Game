package ai_service.repository;

import ai_service.entity.PregeneratedQuest;
import ai_service.entity.enums.PregenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PregeneratedQuestRepository extends JpaRepository<PregeneratedQuest, UUID> {
    List<PregeneratedQuest> findByPlayerIdAndStatus(UUID playerId, PregenStatus status);
    Optional<PregeneratedQuest> findFirstByPlayerIdAndNpcCodeAndStatus(UUID playerId, String npcCode, PregenStatus status);
}
