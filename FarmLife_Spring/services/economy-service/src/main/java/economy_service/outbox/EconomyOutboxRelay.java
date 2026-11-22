package outbox;

import entity.enums.OutboxStatus;
import entity.OutboxEvent;
import repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class EconomyOutboxRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${farmlife.outbox.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${farmlife.outbox.poll-ms:500}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> batch = outboxEventRepository.lockPendingBatch(batchSize);
        for (OutboxEvent event : batch) {
            try {
                String payloadStr = objectMapper.writeValueAsString(event.getPayload());
                kafkaTemplate.send(event.getEventType(), event.getAggregateId(), payloadStr)
                        .get(5, TimeUnit.SECONDS);
                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(Instant.now());
            } catch (Exception ex) {
                int retries = event.getRetryCount() + 1;
                event.setRetryCount(retries);
                if (retries >= 5) {
                    event.setStatus(OutboxStatus.FAILED);
                }
                log.error("Failed publishing economy outbox event {} (attempt {})", event.getEventId(), retries, ex);
            }
        }
        if (!batch.isEmpty()) {
            outboxEventRepository.saveAll(batch);
        }
    }
}
