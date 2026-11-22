package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.Animal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnimalRepository extends JpaRepository<Animal, UUID> {
    List<Animal> findAllByPlayerId(UUID playerId);
    List<Animal> findAllByHousing_ObjectId(UUID housingObjectId);
    Optional<Animal> findByFeedingReservationId(UUID reservationId);
}
