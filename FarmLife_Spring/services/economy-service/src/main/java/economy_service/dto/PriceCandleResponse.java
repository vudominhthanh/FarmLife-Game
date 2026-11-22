package dto;

import java.time.Instant;

public record PriceCandleResponse(
        String itemCode,
        Instant bucketTime,
        Long open,
        Long high,
        Long low,
        Long close,
        Long volume
) {}
