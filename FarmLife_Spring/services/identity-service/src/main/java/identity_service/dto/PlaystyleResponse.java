package dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlaystyleResponse(
        UUID playerId,
        List<String> preferredCropCodes,
        List<String> preferredAnimalCodes,
        String playStyleTag,
        Map<String, Object> tags,
        Instant aiProfiledAt,
        Long version,
        Instant updatedAt
) {}
