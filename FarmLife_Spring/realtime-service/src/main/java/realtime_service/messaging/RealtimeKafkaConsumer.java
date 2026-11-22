package realtime_service.messaging;

import contracts.EventEnvelope;
import contracts.KafkaTopics;
import contracts.InventoryUpdatedEvent;
import contracts.ReservationResolvedEvent;
import realtime_service.redis.RedisWsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeKafkaConsumer {

    private final RedisWsPublisher redisWsPublisher;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.INVENTORY_UPDATED_V1,
            groupId = "${spring.kafka.consumer.group-id:realtime-service}"
    )
    public void onInventoryUpdated(String rawMessage) throws Exception {

        EventEnvelope<InventoryUpdatedEvent> envelope =
                objectMapper.readValue(
                        rawMessage,
                        new TypeReference<>() {}
                );

        InventoryUpdatedEvent payload = envelope.payload();

        redisWsPublisher.pushToUser(
                payload.playerId().toString(),
                "/queue/inventory",
                KafkaTopics.INVENTORY_UPDATED_V1,
                payload
        );

        log.debug(
                "Forwarded inventory update to player {} (slots={}, overflow={})",
                payload.playerId(),
                payload.slots().size(),
                payload.overflowToMailbox()
        );
    }

    @KafkaListener(
            topics = KafkaTopics.RESERVATION_RESOLVED_V1,
            groupId = "${spring.kafka.consumer.group-id:realtime-service}"
    )
    public void onReservationResolved(String rawMessage) throws Exception {

        EventEnvelope<ReservationResolvedEvent> envelope =
                objectMapper.readValue(
                        rawMessage,
                        new TypeReference<>() {}
                );

        ReservationResolvedEvent payload = envelope.payload();

        redisWsPublisher.pushToUser(
                payload.playerId().toString(),
                "/queue/farm",
                KafkaTopics.RESERVATION_RESOLVED_V1,
                payload
        );

        log.debug(
                "Forwarded reservation resolution (success={}) to player {}",
                payload.success(),
                payload.playerId()
        );
    }
}