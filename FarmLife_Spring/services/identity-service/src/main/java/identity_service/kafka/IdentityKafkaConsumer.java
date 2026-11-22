package kafka;

import contracts.*;
import entity.ProcessedEvent;
import entity.ProcessedEventId;
import repository.ProcessedEventRepository;
import service.CustomizationService;
import service.PlayerProfileService;
import service.PlayerStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdentityKafkaConsumer {

    public static final String CONSUMER_GROUP = "identity-service-group";

    private final ProcessedEventRepository processedEventRepository;
    private final PlayerStatService playerStatService;
    private final PlayerProfileService playerProfileService;
    private final CustomizationService customizationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.CROP_HARVESTED, groupId = CONSUMER_GROUP)
    @Transactional
    public void handleCropHarvested(String message) {
        try {
            CropHarvestedEvent event = parsePayload(message, CropHarvestedEvent.class);
            if (isAlreadyProcessed(event.eventId())) {
                log.debug("Event {} already processed by {}", event.eventId(), CONSUMER_GROUP);
                return;
            }

            playerStatService.recordHarvest(event.playerId(), 1);
            if (event.expGained() > 0) {
                playerProfileService.addXp(event.playerId(), event.expGained());
            }

            markProcessed(event.eventId());
            log.info("Processed CropHarvestedEvent {} for player {}", event.eventId(), event.playerId());
        } catch (Exception e) {
            log.error("Failed to handle CropHarvestedEvent message: {}", message, e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = KafkaTopics.TRADE_COMPLETED, groupId = CONSUMER_GROUP)
    @Transactional
    public void handleTradeCompleted(String message) {
        try {
            TradeCompletedEvent event = parsePayload(message, TradeCompletedEvent.class);
            if (isAlreadyProcessed(event.eventId())) {
                log.debug("Event {} already processed by {}", event.eventId(), CONSUMER_GROUP);
                return;
            }

            playerStatService.recordTrade(event.sellerId(), 1, event.totalPrice());

            markProcessed(event.eventId());
            log.info("Processed TradeCompletedEvent {} for seller {}", event.eventId(), event.sellerId());
        } catch (Exception e) {
            log.error("Failed to handle TradeCompletedEvent message: {}", message, e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = KafkaTopics.ASSET_READY, groupId = CONSUMER_GROUP)
    @Transactional
    public void handleAssetReady(String message) {
        try {
            AssetReadyEvent event = parsePayload(message, AssetReadyEvent.class);
            if (isAlreadyProcessed(event.eventId())) {
                log.debug("Event {} already processed by {}", event.eventId(), CONSUMER_GROUP);
                return;
            }

            if ("identity".equalsIgnoreCase(event.ownerService()) || "identity-service".equalsIgnoreCase(event.ownerService())) {
                if (event.assetKey() != null && event.assetKey().contains("avatar")) {
                    UUID playerId = UUID.fromString(event.ownerRef());
                    customizationService.updateCustomAvatarKey(playerId, event.assetKey());
                    log.info("Updated custom avatar for player {} from AssetReadyEvent {}", playerId, event.eventId());
                }
            }

            markProcessed(event.eventId());
        } catch (Exception e) {
            log.error("Failed to handle AssetReadyEvent message: {}", message, e);
            throw new RuntimeException(e);
        }
    }

    private boolean isAlreadyProcessed(UUID eventId) {
        return processedEventRepository.existsById(new ProcessedEventId(eventId, CONSUMER_GROUP));
    }

    private void markProcessed(UUID eventId) {
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .id(new ProcessedEventId(eventId, CONSUMER_GROUP))
                .processedAt(Instant.now())
                .build();
        processedEventRepository.save(processedEvent);
    }

    private <T> T parsePayload(String message, Class<T> targetClass) throws Exception {
        if (message.contains("\"payload\":") && message.contains("\"eventId\":")) {
            // Check if wrapped in EventEnvelope
            var node = objectMapper.readTree(message);
            if (node.has("payload")) {
                return objectMapper.treeToValue(node.get("payload"), targetClass);
            }
        }
        return objectMapper.readValue(message, targetClass);
    }
}
