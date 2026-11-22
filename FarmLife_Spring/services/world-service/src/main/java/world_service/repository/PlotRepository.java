package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.Plot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlotRepository extends JpaRepository<Plot, UUID> {
    List<Plot> findAllByFarm_FarmId(UUID farmId);
    Optional<Plot> findByFarm_FarmIdAndCoordXAndCoordY(UUID farmId, Short coordX, Short coordY);
}
