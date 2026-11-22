package outbox_common;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxPublisherHelper {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public UUID publish(String aggregateType, UUID aggregateId, String topic, Object eventPayload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(eventPayload);
            UUID eventId = UUID.randomUUID();

            OutboxEventEntity outbox = OutboxEventEntity.builder()
                    .id(eventId)
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId.toString())
                    .eventType(eventPayload.getClass().getSimpleName())
                    .topic(topic)
                    .payload(jsonPayload)
                    .status("PENDING")
                    .retryCount(0)
                    .createdAt(Instant.now())
                    .build();

            outboxEventRepository.save(outbox);
            return eventId;
        } catch (JacksonException e) {
            throw new IllegalStateException("Không thể serialize Outbox payload cho topic: " + topic, e);
        }
    }
}