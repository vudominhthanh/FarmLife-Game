package world_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CraftRequest(
        @NotNull
        UUID machineObjectId,

        @NotBlank
        @Size(max = 50)
        String recipeCode,

        @NotNull
        @Min(1)
        Integer quantity
) {}
