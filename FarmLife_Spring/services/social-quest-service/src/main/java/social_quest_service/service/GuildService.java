package social_quest_service.service;

import social_quest_service.dto.GuildCreateRequest;
import social_quest_service.dto.GuildMemberResponse;
import social_quest_service.dto.GuildResponse;

import java.util.List;
import java.util.UUID;

public interface GuildService {
    GuildResponse createGuild(UUID leaderId, GuildCreateRequest request);
    GuildResponse getGuild(UUID guildId);
    GuildResponse getMyGuild(UUID playerId);
    GuildMemberResponse joinGuild(UUID playerId, UUID guildId);
    void leaveGuild(UUID playerId, UUID guildId);
    List<GuildMemberResponse> getGuildMembers(UUID guildId);
}
