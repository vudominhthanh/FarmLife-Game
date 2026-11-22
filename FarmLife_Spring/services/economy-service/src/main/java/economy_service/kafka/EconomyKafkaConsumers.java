package kafka;

import contracts.EventEnvelope;
import contracts.KafkaTopics;
import contracts.CropHarvestedEvent;
import contracts.ItemReserveRequestedEvent;
import contracts.PlayerCreatedEvent;
import contracts.ReservationResolvedEvent;
import dto.ReserveOutcome;
import entity.ProcessedEvent;
import outbox.EconomyOutboxService;
import repository.ProcessedEventRepository;
import service.EconomyInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EconomyKafkaConsumers {

    private final EconomyInventoryService inventoryService;
    private final ProcessedEventRepository processedEventRepository;
    private final EconomyOutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.consumer.group-id:economy-service}")
    private String consumerGroup;

    @KafkaListener(topics = KafkaTopics.PLAYER_CREATED, groupId = "${spring.kafka.consumer.group-id:economy-service}")
    @Transactional
    public void onPlayerCreated(String rawMessage) throws Exception {
        EventEnvelope<PlayerCreatedEvent> envelope = objectMapper.readValue(
                rawMessage, new TypeReference<>() {});

        if (processedEventRepository.existsByEventIdAndConsumerGroup(envelope.eventId(), consumerGroup)) {
            return;
        }

        inventoryService.provisionNewPlayerEconomy(envelope.payload());
        markProcessed(envelope.eventId());
    }

    @KafkaListener(topics = KafkaTopics.ITEM_RESERVE_REQUESTED, groupId = "${spring.kafka.consumer.group-id:economy-service}")
    @Transactional
    public void onItemReserveRequested(String rawMessage) throws Exception {
        EventEnvelope<ItemReserveRequestedEvent> envelope = objectMapper.readValue(
                rawMessage, new TypeReference<>() {});

        if (processedEventRepository.existsByEventIdAndConsumerGroup(envelope.eventId(), consumerGroup)) {
            return;
        }

        ItemReserveRequestedEvent payload = envelope.payload();
        ReserveOutcome outcome = inventoryService.reserveAndCommitItems(payload);
        if (!outcome.success()) {
            log.info("Reservation {} failed for player {}: {}",
                    payload.reservationId(), payload.playerId(), outcome.failureReason());
            outboxService.enqueue(
                    KafkaTopics.RESERVATION_RESOLVED,
                    "RESERVATION",
                    payload.playerId().toString(),
                    new ReservationResolvedEvent(
                            payload.reservationId(),
                            payload.playerId(),
                            payload.reason(),
                            payload.refId(),
                            false,
                            outcome.failureReason(),
                            payload.items()
                    )
            );
        }

        markProcessed(envelope.eventId());
    }

    @KafkaListener(topics = KafkaTopics.CROP_HARVESTED, groupId = "${spring.kafka.consumer.group-id:economy-service}")
    @Transactional
    public void onCropHarvested(String rawMessage) throws Exception {
        EventEnvelope<CropHarvestedEvent> envelope = objectMapper.readValue(
                rawMessage, new TypeReference<>() {});

        if (processedEventRepository.existsByEventIdAndConsumerGroup(envelope.eventId(), consumerGroup)) {
            return;
        }

        inventoryService.handleCropHarvested(envelope.payload());
        markProcessed(envelope.eventId());
    }

    private void markProcessed(UUID eventId) {
        processedEventRepository.save(ProcessedEvent.builder()
                .eventId(eventId)
                .consumerGroup(consumerGroup)
                .build());
    }
}
