package analytics_service.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TrackEventRequest(
        @NotBlank(message = "Event type must not be blank")
        String eventType,
        UUID sessionId,
        Map<String, Object> payload,
        Instant occurredAt
) {}
