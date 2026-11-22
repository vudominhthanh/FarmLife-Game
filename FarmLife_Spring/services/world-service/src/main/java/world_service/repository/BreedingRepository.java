package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.Breeding;

import java.util.List;
import java.util.UUID;

public interface BreedingRepository extends JpaRepository<Breeding, Long> {
    List<Breeding> findAllByParentA_AnimalIdOrParentB_AnimalId(UUID parentA, UUID parentB);
}
