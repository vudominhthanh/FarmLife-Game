package contracts;

import java.time.Instant;
import java.util.UUID;

public record MarketRumorPublishedEvent(
        UUID eventId,
        String itemCode,
        String rumorText,
        String severity,
        String priceTrend,
        Instant publishedAt
) {}
