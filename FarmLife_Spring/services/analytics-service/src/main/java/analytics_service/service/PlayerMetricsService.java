package analytics_service.service;

import analytics_service.dto.PlayerDailyMetricsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PlayerMetricsService {

    PlayerDailyMetricsResponse getTodayMetrics(UUID playerId);

    PlayerDailyMetricsResponse getMetricsForDay(UUID playerId, LocalDate day);

    List<PlayerDailyMetricsResponse> getRecentMetrics(UUID playerId, int limit);

    void incrementMetrics(UUID playerId, LocalDate day, int sessions, int minutes, long coins, int harvests, int trades);
}
