package social_quest_service.dto;

import java.time.Instant;
import java.util.UUID;

public record GuildResponse(
        UUID guildId,
        String name,
        UUID leaderId,
        Integer level,
        Integer maxMembers,
        String description,
        String bannerAssetKey,
        Instant createdAt,
        long currentMemberCount
) {}
