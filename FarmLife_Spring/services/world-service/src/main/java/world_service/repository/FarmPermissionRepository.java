package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.FarmPermission;
import world_service.entity.id.FarmPermissionId;

import java.util.List;
import java.util.UUID;

public interface FarmPermissionRepository extends JpaRepository<FarmPermission, FarmPermissionId> {
    List<FarmPermission> findAllById_FarmId(UUID farmId);
    List<FarmPermission> findAllById_PlayerId(UUID playerId);
}
