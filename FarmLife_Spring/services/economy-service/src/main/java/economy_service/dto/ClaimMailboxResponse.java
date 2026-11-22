package dto;

import java.util.UUID;

public record ClaimMailboxResponse(
        UUID mailItemId,
        String itemCode,
        Integer quantity,
        boolean success,
        String message
) {}
