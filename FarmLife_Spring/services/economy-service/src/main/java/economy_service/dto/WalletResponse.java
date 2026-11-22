package dto;

import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
        UUID walletId,
        UUID playerId,
        long coins,
        long gems,
        long lockedCoins,
        Instant updatedAt
) {}
