package analytics_service.service;

import analytics_service.dto.EventResponse;
import analytics_service.dto.TrackEventRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AnalyticsEventService {

    EventResponse recordEvent(UUID playerId, TrackEventRequest request);

    List<EventResponse> recordBatchEvents(UUID playerId, List<TrackEventRequest> requests);

    List<EventResponse> getPlayerEvents(UUID playerId, int page, int size);

    void recordSystemEvent(UUID playerId, String eventType, Map<String, Object> payload, Instant occurredAt);
}
