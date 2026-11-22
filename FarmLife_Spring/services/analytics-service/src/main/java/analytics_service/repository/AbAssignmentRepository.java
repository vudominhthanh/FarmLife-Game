package analytics_service.repository;

import analytics_service.entity.AbAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AbAssignmentRepository extends JpaRepository<AbAssignment, AbAssignment.AbAssignmentId> {

    Optional<AbAssignment> findByExperimentIdAndPlayerId(UUID experimentId, UUID playerId);

    List<AbAssignment> findByPlayerId(UUID playerId);
}
