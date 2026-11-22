package realtime_service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
@RequiredArgsConstructor
public class RedisWsPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${farmlife.realtime.redis-channel:farmlife:ws:push}")
    private String channel;

    public void pushToUser(
            String playerId,
            String destination,
            String eventType,
            Object payload
    ) {
        try {
            ObjectNode envelope = objectMapper.createObjectNode();

            envelope.put("targetPlayerId", playerId);
            envelope.put("destination", destination);
            envelope.put("eventType", eventType);
            envelope.put(
                    "payloadJson",
                    objectMapper.writeValueAsString(payload)
            );

            redisTemplate.convertAndSend(
                    channel,
                    objectMapper.writeValueAsString(envelope)
            );

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed publishing WebSocket event to Redis",
                    ex
            );
        }
    }

    public void pushToTopic(
            String destination,
            String eventType,
            Object payload
    ) {
        try {
            ObjectNode envelope = objectMapper.createObjectNode();

            envelope.putNull("targetPlayerId");
            envelope.put("destination", destination);
            envelope.put("eventType", eventType);
            envelope.put(
                    "payloadJson",
                    objectMapper.writeValueAsString(payload)
            );

            redisTemplate.convertAndSend(
                    channel,
                    objectMapper.writeValueAsString(envelope)
            );

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed publishing WebSocket topic event to Redis",
                    ex
            );
        }
    }
}