package dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PlayerCreatedEvent(
        UUID eventId,
        UUID playerId,
        UUID accountId,
        String username,
        String displayName,
        String region,
        String language,
        String playStyleTag,
        OffsetDateTime createdAt
) {}