package analytics_service.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AbExperimentResponse(
        UUID experimentId,
        String name,
        Map<String, Object> variants,
        String status,
        Instant startedAt,
        Instant endedAt
) {}
