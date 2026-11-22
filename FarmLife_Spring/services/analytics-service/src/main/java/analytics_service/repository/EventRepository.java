package analytics_service.repository;

import analytics_service.entity.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, Event.EventId> {

    List<Event> findByPlayerIdOrderByOccurredAtDesc(UUID playerId, Pageable pageable);

    List<Event> findByEventTypeOrderByOccurredAtDesc(String eventType, Pageable pageable);
}
