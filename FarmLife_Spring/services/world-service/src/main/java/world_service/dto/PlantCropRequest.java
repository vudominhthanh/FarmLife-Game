package world_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlantCropRequest(
        @NotBlank
        @Size(max = 50)
        String cropCode
) {}
