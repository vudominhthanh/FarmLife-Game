package realtime_service.dto;

import java.time.Instant;

public record WsPushEnvelope(
        String targetPlayerId,
        String destination,
        String eventType,
        String payloadJson,
        Instant emittedAt
) {}
