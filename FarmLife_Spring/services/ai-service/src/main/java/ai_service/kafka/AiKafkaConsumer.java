package ai_service.kafka;

import contracts.KafkaTopics;
import contracts.PlaystyleUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import ai_service.dto.GenerateQuestRequest;
import ai_service.entity.ProcessedEvent;
import ai_service.repository.ProcessedEventRepository;
import ai_service.service.QuestGeneratorService;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiKafkaConsumer {

    public static final String CONSUMER_GROUP = "ai-service-group";

    private final ProcessedEventRepository processedEventRepository;
    private final QuestGeneratorService questGeneratorService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.PLAYSTYLE_UPDATED, groupId = CONSUMER_GROUP)
    @Transactional
    public void handlePlaystyleUpdated(String message) {
        try {
            PlaystyleUpdatedEvent event = parsePayload(message, PlaystyleUpdatedEvent.class);
            if (isAlreadyProcessed(event.eventId())) return;

            log.info("Received playstyle update for player {}: tag={}", event.playerId(), event.playStyleTag());
            questGeneratorService.generatePersonalizedQuest(
                    event.playerId(),
                    new GenerateQuestRequest("mayor_thomas", "Playstyle: " + event.playStyleTag())
            );

            markProcessed(event.eventId());
            log.info("Processed PlaystyleUpdatedEvent and pregenerated quest for player {}", event.playerId());
        } catch (Exception e) {
            log.error("Failed to handle PlaystyleUpdatedEvent in ai-service: {}", message, e);
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
