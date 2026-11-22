package world_service.kafka;

import contracts.KafkaTopics;
import contracts.PlayerCreatedEvent;
import contracts.ReservationResolvedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import world_service.entity.ProcessedEvent;
import world_service.entity.id.ProcessedEventId;
import world_service.repository.ProcessedEventRepository;
import world_service.service.AnimalService;
import world_service.service.CraftingService;
import world_service.service.FarmService;
import world_service.service.PlantingService;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorldKafkaConsumer {

    public static final String CONSUMER_GROUP = "world-service-group";

    private final ProcessedEventRepository processedEventRepository;
    private final FarmService farmService;
    private final PlantingService plantingService;
    private final AnimalService animalService;
    private final CraftingService craftingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.PLAYER_CREATED, groupId = CONSUMER_GROUP)
    @Transactional
    public void handlePlayerCreated(String message) {
        try {
            PlayerCreatedEvent event = parsePayload(message, PlayerCreatedEvent.class);
            if (isAlreadyProcessed(event.eventId())) {
                log.debug("Event {} already processed by {}", event.eventId(), CONSUMER_GROUP);
                return;
            }

            farmService.createInitialFarmForPlayer(event.playerId(), event.displayName() + "'s Farm");
            markProcessed(event.eventId());
            log.info("Provisioned initial farm for player {} from PlayerCreatedEvent {}", event.playerId(), event.eventId());
        } catch (Exception e) {
            log.error("Failed to handle PlayerCreatedEvent message: {}", message, e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = KafkaTopics.RESERVATION_RESOLVED, groupId = CONSUMER_GROUP)
    @Transactional
    public void handleReservationResolved(String message) {
        try {
            ReservationResolvedEvent event = parsePayload(message, ReservationResolvedEvent.class);
            if (isAlreadyProcessed(event.eventId())) {
                log.debug("Event {} already processed by {}", event.eventId(), CONSUMER_GROUP);
                return;
            }

            String reason = event.reason() != null ? event.reason().toUpperCase() : "";
            if (reason.contains("PLANT")) {
                plantingService.handleReservationResolved(event);
            } else if (reason.contains("FEED") || reason.contains("ANIMAL")) {
                animalService.handleFeedingReservationResolved(event);
            } else if (reason.contains("CRAFT")) {
                craftingService.handleCraftReservationResolved(event);
            } else {
                log.info("Unhandled reservation reason: {}", reason);
            }

            markProcessed(event.eventId());
        } catch (Exception e) {
            log.error("Failed to handle ReservationResolvedEvent message: {}", message, e);
            throw new RuntimeException(e);
        }
    }

    private boolean isAlreadyProcessed(UUID eventId) {
        return processedEventRepository.existsById(new ProcessedEventId(eventId, CONSUMER_GROUP));
    }

    private void markProcessed(UUID eventId) {
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .id(new ProcessedEventId(eventId, CONSUMER_GROUP))
                .processedAt(OffsetDateTime.now())
                .build();
        processedEventRepository.save(processedEvent);
    }

    private <T> T parsePayload(String message, Class<T> targetClass) throws Exception {
        if (message.contains("\"payload\":") && message.contains("\"eventId\":")) {
            var node = objectMapper.readTree(message);
            if (node.has("payload")) {
                return objectMapper.treeToValue(node.get("payload"), targetClass);
            }
        }
        return objectMapper.readValue(message, targetClass);
    }
}
