package dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BuyOrderRequest(
        @NotNull
        @Min(1)
        Integer quantity
) {}
