package dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MarketListingRequest(
        @NotBlank
        @Size(max = 50)
        String itemCode,

        @NotNull
        @Min(1)
        Integer quantity,

        @NotNull
        @Min(1)
        Long unitPrice
) {}
