package social_quest_service.kafka;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import contracts.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social_quest_service.dto.SendChatMessageRequest;
import social_quest_service.entity.ProcessedEvent;
import social_quest_service.entity.Quest;
import social_quest_service.entity.enums.QuestSource;
import social_quest_service.entity.enums.QuestType;
import social_quest_service.repository.ProcessedEventRepository;
import social_quest_service.repository.QuestRepository;
import social_quest_service.service.ChatService;
import social_quest_service.service.QuestService;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialQuestKafkaConsumer {

    public static final String CONSUMER_GROUP = "social-quest-service-group";
    private static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final ProcessedEventRepository processedEventRepository;
    private final QuestService questService;
    private final QuestRepository questRepository;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.PLAYER_CREATED, groupId = CONSUMER_GROUP)
    @Transactional
    public void handlePlayerCreated(String message) {
        try {
            PlayerCreatedEvent event = parsePayload(message, PlayerCreatedEvent.class);
            if (isAlreadyProcessed(event.eventId())) return;

            questService.assignStarterQuests(event.playerId());
            markProcessed(event.eventId());
            log.info("Assigned starter quests for new player {}", event.playerId());
        } catch (Exception e) {
            log.error("Failed to handle PlayerCreatedEvent in social-quest: {}", message, e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = KafkaTopics.CROP_HARVESTED, groupId = CONSUMER_GROUP)
    @Transactional
    public void handleCropHarvested(String message) {
        try {
            CropHarvestedEvent event = parsePayload(message, CropHarvestedEvent.class);
            if (isAlreadyProcessed(event.eventId())) return;

            questService.progressQuests(event.playerId(), "HARVEST", event.harvestedQuantity());
            markProcessed(event.eventId());
            log.debug("Processed CropHarvestedEvent for player {}", event.playerId());
        } catch (Exception e) {
            log.error("Failed to handle CropHarvestedEvent in social-quest: {}", message, e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = KafkaTopics.TRADE_COMPLETED, groupId = CONSUMER_GROUP)
    @Transactional
    public void handleTradeCompleted(String message) {
        try {
            TradeCompletedEvent event = parsePayload(message, TradeCompletedEvent.class);
            if (isAlreadyProcessed(event.eventId())) return;

            if (event.sellerId() != null) {
                questService.progressQuests(event.sellerId(), "TRADE", 1);
            }
            if (event.buyerId() != null) {
                questService.progressQuests(event.buyerId(), "TRADE", 1);
            }
            markProcessed(event.eventId());
            log.debug("Processed TradeCompletedEvent for listing {}", event.listingId());
        } catch (Exception e) {
            log.error("Failed to handle TradeCompletedEvent in social-quest: {}", message, e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = KafkaTopics.QUEST_ASSIGNED, groupId = CONSUMER_GROUP)
    @Transactional
    public void handleQuestAssigned(String message) {
        try {
            QuestAssignedEvent event = parsePayload(message, QuestAssignedEvent.class);
            if (isAlreadyProcessed(event.eventId())) return;

            if (!questRepository.existsById(event.questCode())) {
                Map<String, Object> rewardMap = Map.of();
                if (event.rewardJson() != null && !event.rewardJson().isBlank()) {
                    rewardMap = objectMapper.readValue(event.rewardJson(), new TypeReference<Map<String, Object>>() {});
                }

                Quest newQuest = Quest.builder()
                        .questCode(event.questCode())
                        .title(event.title() != null ? event.title() : "AI Mission")
                        .description("Personalized quest from " + event.sourceNpcCode())
                        .type(QuestType.DAILY)
                        .goalType(event.goalType() != null ? event.goalType() : "ANY")
                        .goalTarget(event.goalTarget() > 0 ? event.goalTarget() : 1)
                        .rewardJson(rewardMap)
                        .isPersonalized(true)
                        .source(QuestSource.AI_GENERATED)
                        .sourceNpcCode(event.sourceNpcCode())
                        .pregenId(event.pregenId())
                        .isActive(true)
                        .build();

                questRepository.save(newQuest);
            }

            questService.assignQuest(event.playerId(), event.questCode());
            markProcessed(event.eventId());
            log.info("Assigned AI quest {} to player {}", event.questCode(), event.playerId());
        } catch (Exception e) {
            log.error("Failed to handle QuestAssignedEvent in social-quest: {}", message, e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = KafkaTopics.MARKET_RUMOR_PUBLISHED, groupId = CONSUMER_GROUP)
    @Transactional
    public void handleMarketRumorPublished(String message) {
        try {
            MarketRumorPublishedEvent event = parsePayload(message, MarketRumorPublishedEvent.class);
            if (isAlreadyProcessed(event.eventId())) return;

            String rumorChat = String.format("[MARKET RUMOR] %s: %s (Trend: %s)",
                    event.itemCode(), event.rumorText(), event.priceTrend());
            chatService.sendMessage(SYSTEM_UUID, new SendChatMessageRequest("GLOBAL", rumorChat));

            markProcessed(event.eventId());
            log.info("Broadcasted market rumor to GLOBAL chat for item {}", event.itemCode());
        } catch (Exception e) {
            log.error("Failed to handle MarketRumorPublishedEvent in social-quest: {}", message, e);
            throw new RuntimeException(e);
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
