package analytics_service.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EventResponse(
        UUID eventId,
        UUID playerId,
        String eventType,
        UUID sessionId,
        Map<String, Object> payload,
        Instant occurredAt
) {}
