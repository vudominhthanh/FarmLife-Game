package analytics_service.dto;

import java.time.Instant;
import java.util.UUID;

public record AbAssignmentResponse(
        UUID experimentId,
        String experimentName,
        UUID playerId,
        String variant,
        Instant assignedAt
) {}
