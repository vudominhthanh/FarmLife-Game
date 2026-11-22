package contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        String aggregateType,
        String aggregateId,
        OffsetDateTime occurredAt,
        T payload
) {
    public EventEnvelope(UUID eventId, String eventType, String aggregateId, OffsetDateTime occurredAt, T payload) {
        this(eventId, eventType, "", aggregateId, occurredAt, payload);
    }

    public static <T> EventEnvelope<T> of(UUID eventId, String eventType, String aggregateType, String aggregateId, T payload) {
        return new EventEnvelope<>(eventId, eventType, aggregateType, aggregateId, OffsetDateTime.now(), payload);
    }

    public static <T> EventEnvelope<T> of(UUID eventId, String eventType, String aggregateId, T payload) {
        return new EventEnvelope<>(eventId, eventType, "", aggregateId, OffsetDateTime.now(), payload);
    }
}
