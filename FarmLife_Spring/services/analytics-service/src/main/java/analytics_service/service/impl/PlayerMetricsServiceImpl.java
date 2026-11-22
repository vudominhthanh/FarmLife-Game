package analytics_service.service.impl;

import analytics_service.dto.PlayerDailyMetricsResponse;
import analytics_service.entity.PlayerDailyMetric;
import analytics_service.mapper.AnalyticsMapper;
import analytics_service.repository.PlayerDailyMetricRepository;
import analytics_service.service.PlayerMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerMetricsServiceImpl implements PlayerMetricsService {

    private final PlayerDailyMetricRepository metricRepository;
    private final AnalyticsMapper analyticsMapper;

    @Override
    @Transactional(readOnly = true)
    public PlayerDailyMetricsResponse getTodayMetrics(UUID playerId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return getMetricsForDay(playerId, today);
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerDailyMetricsResponse getMetricsForDay(UUID playerId, LocalDate day) {
        return metricRepository.findByPlayerIdAndDay(playerId, day)
                .map(analyticsMapper::toMetricsResponse)
                .orElseGet(() -> new PlayerDailyMetricsResponse(playerId, day, 0, 0, 0L, 0, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerDailyMetricsResponse> getRecentMetrics(UUID playerId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, Math.min(30, Math.max(1, limit)));
        return metricRepository.findByPlayerIdOrderByDayDesc(playerId, pageRequest)
                .stream()
                .map(analyticsMapper::toMetricsResponse)
                .toList();
    }

    @Override
    @Transactional
    public void incrementMetrics(UUID playerId, LocalDate day, int sessions, int minutes, long coins, int harvests, int trades) {
        metricRepository.upsertIncrementMetrics(playerId, day, sessions, minutes, coins, harvests, trades);
        log.debug("Incremented metrics for player {} on {}: sessions={}, harvests={}, trades={}, coins={}",
                playerId, day, sessions, harvests, trades, coins);
    }
}
