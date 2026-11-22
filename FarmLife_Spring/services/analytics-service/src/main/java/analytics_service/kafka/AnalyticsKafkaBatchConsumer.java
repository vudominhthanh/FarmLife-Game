package analytics_service.kafka;

import analytics_service.entity.Event;
import analytics_service.entity.ProcessedEvent;
import analytics_service.repository.EventRepository;
import analytics_service.repository.ProcessedEventRepository;
import analytics_service.service.PlayerMetricsService;
import contracts.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsKafkaBatchConsumer {

    public static final String CONSUMER_GROUP = "analytics-service-group";

    private final EventRepository eventRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final PlayerMetricsService playerMetricsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {
                    KafkaTopics.PLAYER_CREATED_V1,
                    KafkaTopics.PLAYSTYLE_UPDATED_V1,
                    KafkaTopics.CROP_HARVESTED_V1,
                    KafkaTopics.ITEM_RESERVE_REQUESTED_V1,
                    KafkaTopics.RESERVATION_RESOLVED_V1,
                    KafkaTopics.INVENTORY_UPDATED_V1,
                    KafkaTopics.TRADE_COMPLETED_V1,
                    KafkaTopics.MARKET_PRICE_TICK_V1,
                    KafkaTopics.ASSET_READY_V1,
                    KafkaTopics.QUEST_ASSIGNED_V1,
                    KafkaTopics.NPC_STATE_CHANGED_V1,
                    KafkaTopics.MARKET_RUMOR_PUBLISHED_V1
            },
            groupId = CONSUMER_GROUP
    )
    @Transactional
    public void consumeBatch(List<ConsumerRecord<String, String>> records) {
        log.info("Analytics consumer received batch of {} records", records.size());
        for (ConsumerRecord<String, String> record : records) {
            try {
                processSingleRecord(record);
            } catch (Exception e) {
                log.error("Failed to process analytics record from topic {}: {}", record.topic(), record.value(), e);
            }
        }
    }

    private void processSingleRecord(ConsumerRecord<String, String> record) {
        String rawValue = record.value();
        if (rawValue == null || rawValue.isBlank()) {
            return;
        }

        UUID eventId = extractEventId(record);
        if (isAlreadyProcessed(eventId)) {
            log.debug("Event {} already processed by {}, skipping", eventId, CONSUMER_GROUP);
            return;
        }

        String eventType = record.topic();
        Instant occurredAt = Instant.ofEpochMilli(record.timestamp());
        if (occurredAt.isBefore(Instant.ofEpochMilli(0))) {
            occurredAt = Instant.now();
        }

        UUID playerId = null;
        UUID sessionId = null;
        Map<String, Object> payloadMap = new HashMap<>();

        try {
            JsonNode root = objectMapper.readTree(rawValue);
            if (root.has("eventId") && root.get("eventId").isTextual()) {
                try {
                    eventId = UUID.fromString(root.get("eventId").asText());
                } catch (IllegalArgumentException ignored) {}
            }
            if (root.has("eventType") && root.get("eventType").isTextual()) {
                eventType = root.get("eventType").asText();
            }
            if (root.has("occurredAt") && root.get("occurredAt").isTextual()) {
                try {
                    occurredAt = Instant.parse(root.get("occurredAt").asText());
                } catch (Exception ignored) {}
            }

            JsonNode payloadNode = root.has("payload") ? root.get("payload") : root;
            payloadMap = objectMapper.convertValue(payloadNode, new TypeReference<Map<String, Object>>() {});

            if (payloadNode.has("playerId") && payloadNode.get("playerId").isTextual()) {
                playerId = parseUuidSilently(payloadNode.get("playerId").asText());
            } else if (payloadNode.has("player_id") && payloadNode.get("player_id").isTextual()) {
                playerId = parseUuidSilently(payloadNode.get("player_id").asText());
            }

            if (payloadNode.has("sessionId") && payloadNode.get("sessionId").isTextual()) {
                sessionId = parseUuidSilently(payloadNode.get("sessionId").asText());
            }
        } catch (Exception e) {
            log.warn("Could not parse JSON payload from record: {}", rawValue);
            payloadMap.put("raw", rawValue);
        }

        // Persist event into partitioned analytics.events
        Event event = Event.builder()
                .eventId(eventId)
                .playerId(playerId)
                .eventType(eventType)
                .sessionId(sessionId)
                .payload(payloadMap)
                .occurredAt(occurredAt)
                .build();
        eventRepository.save(event);

        // Update KPI daily metrics if player is identified
        if (playerId != null) {
            LocalDate day = occurredAt.atOffset(ZoneOffset.UTC).toLocalDate();
            updateKpiMetrics(record.topic(), eventType, playerId, day, payloadMap);
        }

        // Mark inbox processed
        markProcessed(eventId);
    }

    private void updateKpiMetrics(String topic, String eventType, UUID playerId, LocalDate day, Map<String, Object> payload) {
        if (topic.contains("crop-harvested") || eventType.contains("crop-harvested")) {
            playerMetricsService.incrementMetrics(playerId, day, 0, 0, 0L, 1, 0);
        } else if (topic.contains("trade-completed") || eventType.contains("trade-completed")) {
            long coins = 0L;
            if (payload.containsKey("totalPrice") && payload.get("totalPrice") instanceof Number n) {
                coins = n.longValue();
            } else if (payload.containsKey("coins") && payload.get("coins") instanceof Number n) {
                coins = n.longValue();
            }
            playerMetricsService.incrementMetrics(playerId, day, 0, 0, coins, 0, 1);
        } else if (topic.contains("player-created") || eventType.contains("player-created")) {
            playerMetricsService.incrementMetrics(playerId, day, 1, 0, 0L, 0, 0);
        }
    }

    private UUID extractEventId(ConsumerRecord<String, String> record) {
        org.apache.kafka.common.header.Header header = record.headers().lastHeader("X-Event-Id");
        if (header != null && header.value() != null) {
            try {
                return UUID.fromString(new String(header.value(), StandardCharsets.UTF_8));
            } catch (IllegalArgumentException ignored) {}
        }
        return UUID.randomUUID();
    }

    private UUID parseUuidSilently(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAlreadyProcessed(UUID eventId) {
        return processedEventRepository.existsById(new ProcessedEvent.ProcessedEventId(eventId, CONSUMER_GROUP));
    }

    private void markProcessed(UUID eventId) {
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(eventId)
                .consumerGroup(CONSUMER_GROUP)
                .processedAt(Instant.now())
                .build();
        processedEventRepository.save(processedEvent);
    }
}
