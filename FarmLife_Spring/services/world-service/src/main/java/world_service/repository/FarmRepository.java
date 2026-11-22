package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.Farm;

import java.util.Optional;
import java.util.UUID;

public interface FarmRepository extends JpaRepository<Farm, UUID> {
    Optional<Farm> findByPlayerId(UUID playerId);
}
