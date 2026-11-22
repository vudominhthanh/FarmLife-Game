package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.PlacedObject;
import world_service.entity.enums.ObjectType;

import java.util.List;
import java.util.UUID;

public interface PlacedObjectRepository extends JpaRepository<PlacedObject, UUID> {
    List<PlacedObject> findAllByFarm_FarmId(UUID farmId);
    List<PlacedObject> findAllByFarm_FarmIdAndObjectType(UUID farmId, ObjectType objectType);
}
