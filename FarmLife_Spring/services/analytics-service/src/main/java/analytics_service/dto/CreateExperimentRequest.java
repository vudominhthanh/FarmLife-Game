package analytics_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

public record CreateExperimentRequest(
        @NotBlank(message = "Experiment name must not be blank")
        String name,
        @NotNull(message = "Variants definition must not be null")
        Map<String, Object> variants,
        Instant startedAt,
        Instant endedAt
) {}
