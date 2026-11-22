package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.ActionReplayLog;
import world_service.entity.id.ActionReplayLogId;

import java.util.List;
import java.util.UUID;

public interface ActionReplayLogRepository extends JpaRepository<ActionReplayLog, ActionReplayLogId> {
    List<ActionReplayLog> findAllById_PlayerId(UUID playerId);
}
