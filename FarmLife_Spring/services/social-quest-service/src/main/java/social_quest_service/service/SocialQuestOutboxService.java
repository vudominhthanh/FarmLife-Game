package social_quest_service.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import social_quest_service.entity.OutboxEvent;
import social_quest_service.entity.enums.OutboxStatus;
import social_quest_service.repository.OutboxEventRepository;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialQuestOutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public <T> void enqueue(String aggregateType, String aggregateId, String eventType, T payload) {
        Map<String, Object> payloadMap;
        if (payload instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> casted = (Map<String, Object>) payload;
            payloadMap = casted;
        } else {
            payloadMap = objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {});
        }

        OutboxEvent event = OutboxEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payloadMap)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();

        outboxEventRepository.save(event);
        log.debug("Enqueued outbox event {} for aggregate {}/{}", event.getEventId(), aggregateType, aggregateId);
    }
}
