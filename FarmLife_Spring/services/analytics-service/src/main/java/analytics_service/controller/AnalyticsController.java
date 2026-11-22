package analytics_service.controller;

import analytics_service.dto.*;
import analytics_service.service.AnalyticsEventService;
import analytics_service.service.PlayerMetricsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsEventService eventService;
    private final PlayerMetricsService metricsService;

    @PostMapping("/events")
    public ResponseEntity<EventResponse> trackEvent(
            @RequestHeader(value = "X-Player-Id", required = false) String playerIdStr,
            @Valid @RequestBody TrackEventRequest request
    ) {
        UUID playerId = playerIdStr != null && !playerIdStr.isBlank() ? UUID.fromString(playerIdStr) : null;
        EventResponse response = eventService.recordEvent(playerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/events/batch")
    public ResponseEntity<List<EventResponse>> trackBatchEvents(
            @RequestHeader(value = "X-Player-Id", required = false) String playerIdStr,
            @Valid @RequestBody BatchTrackEventsRequest request
    ) {
        UUID playerId = playerIdStr != null && !playerIdStr.isBlank() ? UUID.fromString(playerIdStr) : null;
        List<EventResponse> responses = eventService.recordBatchEvents(playerId, request.events());
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/events/me")
    public ResponseEntity<List<EventResponse>> getMyEvents(
            @RequestHeader("X-Player-Id") String playerIdStr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID playerId = UUID.fromString(playerIdStr);
        return ResponseEntity.ok(eventService.getPlayerEvents(playerId, page, size));
    }

    @GetMapping("/metrics/me")
    public ResponseEntity<PlayerDailyMetricsResponse> getMyTodayMetrics(
            @RequestHeader("X-Player-Id") String playerIdStr
    ) {
        UUID playerId = UUID.fromString(playerIdStr);
        return ResponseEntity.ok(metricsService.getTodayMetrics(playerId));
    }

    @GetMapping("/metrics/me/recent")
    public ResponseEntity<List<PlayerDailyMetricsResponse>> getMyRecentMetrics(
            @RequestHeader("X-Player-Id") String playerIdStr,
            @RequestParam(defaultValue = "7") int days
    ) {
        UUID playerId = UUID.fromString(playerIdStr);
        return ResponseEntity.ok(metricsService.getRecentMetrics(playerId, days));
    }

    @GetMapping("/metrics/player/{playerId}")
    public ResponseEntity<PlayerDailyMetricsResponse> getPlayerMetricsByDate(
            @PathVariable UUID playerId,
            @RequestParam(required = false) String date
    ) {
        LocalDate day = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(metricsService.getMetricsForDay(playerId, day));
    }
}
