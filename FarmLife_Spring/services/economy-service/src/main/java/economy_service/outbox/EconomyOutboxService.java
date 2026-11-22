package outbox;

import contracts.EventEnvelope;
import entity.OutboxEvent;
import entity.enums.OutboxStatus;
import repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EconomyOutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public <T> void enqueue(String topic, String aggregateType, String aggregateId, T payload) {
        UUID eventId = UUID.randomUUID();
        EventEnvelope<T> envelope = EventEnvelope.of(eventId, topic, aggregateType, aggregateId, payload);
        try {
            Map<String, Object> payloadMap = objectMapper.convertValue(envelope, Map.class);
            OutboxEvent event = new OutboxEvent();
            event.setEventId(eventId);
            event.setAggregateType(aggregateType);
            event.setAggregateId(aggregateId);
            event.setEventType(topic);
            event.setPayload(payloadMap);
            event.setStatus(OutboxStatus.PENDING);
            event.setRetryCount(0);
            outboxEventRepository.save(event);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize economy outbox payload for topic " + topic, e);
        }
    }
}
