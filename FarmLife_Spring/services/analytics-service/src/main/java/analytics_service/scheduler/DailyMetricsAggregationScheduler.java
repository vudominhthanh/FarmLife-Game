package analytics_service.scheduler;

import analytics_service.repository.EventRepository;
import analytics_service.repository.PlayerDailyMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyMetricsAggregationScheduler {

    private final PlayerDailyMetricRepository metricRepository;
    private final EventRepository eventRepository;

    /**
     * Run every hour to log analytics ingestion status.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void logHourlyMetricsStatus() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        long totalEvents = eventRepository.count();
        log.info("Analytics Hourly Check: Today is {}. Total events in analytics store: {}", today, totalEvents);
    }
}
