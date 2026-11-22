package ai_service.repository;

import ai_service.entity.NpcAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NpcAgentRepository extends JpaRepository<NpcAgent, String> {
    Optional<NpcAgent> findByNpcCode(String npcCode);
    List<NpcAgent> findByIsActiveTrue();
}
