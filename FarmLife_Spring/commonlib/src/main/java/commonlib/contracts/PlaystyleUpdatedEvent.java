package contracts;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaystyleUpdatedEvent(
        UUID eventId,
        UUID playerId,
        String playStyleTag,
        List<String> preferredCropCodes,
        List<String> preferredAnimalCodes,
        Instant updatedAt
) {}
