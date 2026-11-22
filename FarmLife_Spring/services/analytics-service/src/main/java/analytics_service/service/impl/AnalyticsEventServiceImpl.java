package analytics_service.service.impl;

import analytics_service.dto.EventResponse;
import analytics_service.dto.TrackEventRequest;
import analytics_service.entity.Event;
import analytics_service.mapper.AnalyticsMapper;
import analytics_service.repository.EventRepository;
import analytics_service.service.AnalyticsEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsEventServiceImpl implements AnalyticsEventService {

    private final EventRepository eventRepository;
    private final AnalyticsMapper analyticsMapper;

    @Override
    @Transactional
    public EventResponse recordEvent(UUID playerId, TrackEventRequest request) {
        Instant occurredAt = request.occurredAt() != null ? request.occurredAt() : Instant.now();
        Event event = Event.builder()
                .playerId(playerId)
                .eventType(request.eventType())
                .sessionId(request.sessionId())
                .payload(request.payload() != null ? request.payload() : new HashMap<>())
                .occurredAt(occurredAt)
                .build();

        Event saved = eventRepository.save(event);
        log.debug("Recorded event '{}' for player {}", saved.getEventType(), playerId);
        return analyticsMapper.toEventResponse(saved);
    }

    @Override
    @Transactional
    public List<EventResponse> recordBatchEvents(UUID playerId, List<TrackEventRequest> requests) {
        List<Event> eventsToSave = new ArrayList<>(requests.size());
        for (TrackEventRequest req : requests) {
            Instant occurredAt = req.occurredAt() != null ? req.occurredAt() : Instant.now();
            eventsToSave.add(Event.builder()
                    .playerId(playerId)
                    .eventType(req.eventType())
                    .sessionId(req.sessionId())
                    .payload(req.payload() != null ? req.payload() : new HashMap<>())
                    .occurredAt(occurredAt)
                    .build());
        }

        List<Event> savedEvents = eventRepository.saveAll(eventsToSave);
        log.info("Batch recorded {} events for player {}", savedEvents.size(), playerId);
        return savedEvents.stream().map(analyticsMapper::toEventResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getPlayerEvents(UUID playerId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return eventRepository.findByPlayerIdOrderByOccurredAtDesc(playerId, pageRequest)
                .stream()
                .map(analyticsMapper::toEventResponse)
                .toList();
    }

    @Override
    @Transactional
    public void recordSystemEvent(UUID playerId, String eventType, Map<String, Object> payload, Instant occurredAt) {
        Event event = Event.builder()
                .playerId(playerId)
                .eventType(eventType)
                .payload(payload != null ? payload : new HashMap<>())
                .occurredAt(occurredAt != null ? occurredAt : Instant.now())
                .build();
        eventRepository.save(event);
    }
}
