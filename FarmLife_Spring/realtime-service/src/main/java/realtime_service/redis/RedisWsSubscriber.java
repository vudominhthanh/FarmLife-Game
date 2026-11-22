package realtime_service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisWsSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String raw = new String(
                    message.getBody(),
                    StandardCharsets.UTF_8
            );

            JsonNode envelope = objectMapper.readTree(raw);

            String targetPlayerId = getText(envelope, "targetPlayerId");
            String destination = getText(envelope, "destination");
            String payloadJson = getText(envelope, "payloadJson");

            if (destination == null || payloadJson == null) {
                log.warn("Invalid Redis WebSocket envelope: {}", raw);
                return;
            }

            if (targetPlayerId != null && !targetPlayerId.isBlank()) {
                messagingTemplate.convertAndSendToUser(
                        targetPlayerId,
                        destination,
                        payloadJson
                );
            } else {
                messagingTemplate.convertAndSend(
                        destination,
                        payloadJson
                );
            }

            log.debug(
                    "Redis event dispatched: destination={}, target={}",
                    destination,
                    targetPlayerId
            );

        } catch (Exception ex) {
            log.error(
                    "Failed dispatching Redis Pub/Sub WebSocket message",
                    ex
            );
        }
    }

    private String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {
            return null;
        }

        return value.asString();
    }
}