package outbox_common;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InboxGuard {

    private final ProcessedEventRepository processedEventRepository;

    /**
     * Checks if an event has already been processed by consumerGroup.
     * If not, inserts a processed_events record within the current transaction and returns true.
     * If already processed or conflict occurs, returns false.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean tryProcess(UUID eventId, String consumerGroup) {
        ProcessedEventId id = new ProcessedEventId(eventId, consumerGroup);
        if (processedEventRepository.existsById(id)) {
            return false;
        }
        try {
            ProcessedEventEntity entity = new ProcessedEventEntity(id, Instant.now());
            processedEventRepository.save(entity);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}
