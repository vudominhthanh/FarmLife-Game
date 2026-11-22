package dto;

import java.util.UUID;

public record WalletBalanceResponse(
        UUID playerId,
        Long gold,
        Long gems,
        Long version
) {}
