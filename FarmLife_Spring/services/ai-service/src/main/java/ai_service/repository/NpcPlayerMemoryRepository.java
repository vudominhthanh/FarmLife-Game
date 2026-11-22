package ai_service.repository;

import ai_service.entity.NpcPlayerMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NpcPlayerMemoryRepository extends JpaRepository<NpcPlayerMemory, UUID> {
    Optional<NpcPlayerMemory> findByNpcCodeAndPlayerId(String npcCode, UUID playerId);
}
