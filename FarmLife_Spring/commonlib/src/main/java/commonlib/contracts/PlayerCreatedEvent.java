package contracts;

import java.time.Instant;
import java.util.UUID;

public record PlayerCreatedEvent(
        UUID eventId,
        UUID accountId,
        UUID playerId,
        String username,
        String displayName,
        Instant occurredAt
) {}