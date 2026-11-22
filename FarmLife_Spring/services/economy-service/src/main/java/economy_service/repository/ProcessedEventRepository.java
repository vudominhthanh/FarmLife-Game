package repository;

import entity.ProcessedEvent;
import entity.ProcessedEvent.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEventId> {
    boolean existsByEventIdAndConsumerGroup(UUID eventId, String consumerGroup);
}
