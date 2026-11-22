package contracts;

import java.time.Instant;
import java.util.UUID;

public record MarketPriceTickEvent(
        UUID eventId,
        String itemCode,
        long oldPrice,
        long newPrice,
        double percentChange,
        long supplyVolume24h,
        long demandVolume24h,
        Instant tickedAt
) {}
