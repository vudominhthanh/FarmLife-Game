package dto;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(
        String deviceId,
        UUID playerId,
        String platform,
        String pushToken,
        Instant firstSeenAt,
        Instant lastSeenAt
) {}
