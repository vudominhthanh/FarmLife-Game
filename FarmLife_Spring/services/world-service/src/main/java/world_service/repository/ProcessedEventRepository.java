package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.ProcessedEvent;
import world_service.entity.id.ProcessedEventId;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEventId> {}
