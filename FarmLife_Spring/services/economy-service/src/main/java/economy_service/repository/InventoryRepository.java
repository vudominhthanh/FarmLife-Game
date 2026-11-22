package repository;

import entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Optional<Inventory> findByPlayerId(UUID playerId);
    boolean existsByPlayerId(UUID playerId);
}
