package ai_service.scheduler;

import contracts.EventEnvelope;
import contracts.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import ai_service.entity.OutboxEvent;
import ai_service.entity.enums.OutboxStatus;
import ai_service.repository.OutboxEventRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiOutboxRelayScheduler {

    private static final int MAX_RETRY = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${farmlife.outbox.relay-interval-ms:2000}")
    @Transactional
    public void relayOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING,
                PageRequest.of(0, 50)
        );

        if (pendingEvents.isEmpty()) return;

        for (OutboxEvent event : pendingEvents) {
            try {
                String topic = resolveTopic(event.getEventType());
                EventEnvelope<Object> envelope = new EventEnvelope<>(
                        event.getEventId(),
                        event.getEventType(),
                        event.getAggregateType(),
                        event.getAggregateId(),
                        OffsetDateTime.now(),
                        event.getPayload()
                );

                String payloadJson = objectMapper.writeValueAsString(envelope);
                kafkaTemplate.send(topic, event.getAggregateId(), payloadJson).get();

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(Instant.now());
                outboxEventRepository.save(event);
                log.debug("Successfully relayed outbox event {} to topic {}", event.getEventId(), topic);
            } catch (Exception ex) {
                log.error("Failed to relay outbox event {}", event.getEventId(), ex);
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= MAX_RETRY) {
                    event.setStatus(OutboxStatus.DEAD_LETTER);
                }
                outboxEventRepository.save(event);
            }
        }
    }

    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "quest-assigned.v1" -> KafkaTopics.QUEST_ASSIGNED;
            case "npc-state-changed.v1" -> KafkaTopics.NPC_STATE_CHANGED;
            case "market-rumor-published.v1" -> KafkaTopics.MARKET_RUMOR_PUBLISHED;
            default -> "farmlife.ai." + eventType;
        };
    }
}
