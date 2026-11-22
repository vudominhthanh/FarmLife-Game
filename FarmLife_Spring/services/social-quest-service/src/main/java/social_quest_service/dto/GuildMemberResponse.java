package social_quest_service.dto;

import social_quest_service.entity.enums.GuildRole;

import java.time.Instant;
import java.util.UUID;

public record GuildMemberResponse(
        UUID guildId,
        UUID playerId,
        GuildRole role,
        Instant joinedAt
) {}
