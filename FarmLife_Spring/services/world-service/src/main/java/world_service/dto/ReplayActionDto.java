package world_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record ReplayActionDto(
        @NotNull
        Long clientSeq,

        @NotBlank
        @Size(max = 50)
        String actionType,

        @NotNull
        UUID idempotencyKey,

        Map<String, Object> payload
) {}
