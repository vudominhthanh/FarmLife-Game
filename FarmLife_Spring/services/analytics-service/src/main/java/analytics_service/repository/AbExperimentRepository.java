package analytics_service.repository;

import analytics_service.entity.AbExperiment;
import analytics_service.entity.enums.ExperimentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AbExperimentRepository extends JpaRepository<AbExperiment, UUID> {

    Optional<AbExperiment> findByName(String name);

    List<AbExperiment> findByStatus(ExperimentStatus status);
}
