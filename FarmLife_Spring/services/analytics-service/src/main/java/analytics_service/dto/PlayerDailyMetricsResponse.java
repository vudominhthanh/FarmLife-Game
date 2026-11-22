package analytics_service.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PlayerDailyMetricsResponse(
        UUID playerId,
        LocalDate day,
        int sessions,
        int minutesPlayed,
        long coinsEarned,
        int harvests,
        int trades
) {}
