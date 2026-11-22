package scheduler;

import contracts.EventEnvelope;
import contracts.KafkaTopics;
import tools.jackson.databind.ObjectMapper;
import entity.OutboxEvent;
import entity.enums.OutboxStatus;
import repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayScheduler {

    private static final short MAX_RETRY = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${farmlife.outbox.poll-interval-ms:500}")
    @Transactional
    public void publishPendingOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PageRequest.of(0, 100));

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

                String jsonMessage = objectMapper.writeValueAsString(envelope);

                // Gửi đồng bộ trong batch nhỏ để đảm bảo broker đã ghi nhận trước khi đổi PUBLISHED
                kafkaTemplate.send(topic, event.getAggregateId(), jsonMessage).get();

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(Instant.now());
                log.info("Đã phát sự kiện Outbox eventId={} tới topic={}", event.getEventId(), topic);
            } catch (Exception ex) {
                short nextRetry = (short) (event.getRetryCount() + 1);
                event.setRetryCount(nextRetry);
                if (nextRetry >= MAX_RETRY) {
                    event.setStatus(OutboxStatus.DEAD_LETTER);
                    log.error("Sự kiện Outbox eventId={} chuyển sang DEAD_LETTER sau {} lần thử", event.getEventId(), nextRetry, ex);
                } else {
                    log.warn("Lỗi phát Outbox eventId={}, lần thử {}/{}: {}", event.getEventId(), nextRetry, MAX_RETRY, ex.getMessage());
                }
            }
        }
    }

    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "PlayerCreatedEvent", "PlayerCreated" -> KafkaTopics.PLAYER_CREATED_V1;
            case "PlaystyleUpdatedEvent", "PlaystyleUpdated" -> KafkaTopics.PLAYSTYLE_UPDATED_V1;
            case "CropHarvestedEvent", "CropHarvested" -> KafkaTopics.CROP_HARVESTED_V1;
            case "ItemReserveRequestedEvent", "ItemReserveRequested" -> KafkaTopics.ITEM_RESERVE_REQUESTED_V1;
            case "ReservationResolvedEvent", "ReservationResolved" -> KafkaTopics.RESERVATION_RESOLVED_V1;
            case "InventoryUpdatedEvent", "InventoryUpdated" -> KafkaTopics.INVENTORY_UPDATED_V1;
            case "TradeCompletedEvent", "TradeCompleted" -> KafkaTopics.TRADE_COMPLETED_V1;
            case "MarketPriceTickEvent", "MarketPriceTick" -> KafkaTopics.MARKET_PRICE_TICK_V1;
            case "AssetReadyEvent", "AssetReady" -> KafkaTopics.ASSET_READY_V1;
            case "QuestAssignedEvent", "QuestAssigned" -> KafkaTopics.QUEST_ASSIGNED_V1;
            case "NpcStateChangedEvent", "NpcStateChanged" -> KafkaTopics.NPC_STATE_CHANGED_V1;
            case "MarketRumorPublishedEvent", "MarketRumorPublished" -> KafkaTopics.MARKET_RUMOR_PUBLISHED_V1;
            default -> eventType;
        };
    }
}
