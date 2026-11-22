package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.ActivePlanting;

import java.util.Optional;
import java.util.UUID;

public interface ActivePlantingRepository extends JpaRepository<ActivePlanting, UUID> {
    Optional<ActivePlanting> findByPlot_PlotId(UUID plotId);
    Optional<ActivePlanting> findByReservationId(UUID reservationId);
}
