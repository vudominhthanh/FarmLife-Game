package social_quest_service.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(
        Long msgId,
        Instant sentAt,
        String channel,
        UUID senderId,
        String content
) {}
