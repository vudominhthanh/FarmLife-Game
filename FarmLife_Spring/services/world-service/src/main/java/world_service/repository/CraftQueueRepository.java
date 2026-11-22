package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.CraftQueue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CraftQueueRepository extends JpaRepository<CraftQueue, UUID> {
    List<CraftQueue> findAllByPlayerId(UUID playerId);
    List<CraftQueue> findAllByMachine_ObjectId(UUID machineObjectId);
    Optional<CraftQueue> findByReservationId(UUID reservationId);
}
