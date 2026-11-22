package world_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record PlacedObjectRequest(
        @NotBlank
        @Size(max = 50)
        String objectCode,

        @NotBlank
        @Size(max = 30)
        String objectType,

        @NotNull
        Integer originX,

        @NotNull
        Integer originY,

        @Positive
        @Max(32767)
        Short width,

        @Positive
        @Max(32767)
        Short height,

        @Min(0)
        @Max(3)
        Short direction,

        Integer maxCapacity,

        Map<String, Object> stateMetadata
) {}
