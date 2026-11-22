package dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketTickerResponse(
        String itemCode,
        Long basePrice,
        Long minPrice,
        Long maxPrice,
        Long currentPrice,
        Long supplyVolume24h,
        Long demandVolume24h,
        BigDecimal volatilityFactor,
        Instant updatedAt
) {}
