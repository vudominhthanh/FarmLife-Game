package social_quest_service.service.impl;

import exception.BusinessException;
import exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social_quest_service.dto.GuildCreateRequest;
import social_quest_service.dto.GuildMemberResponse;
import social_quest_service.dto.GuildResponse;
import social_quest_service.entity.Guild;
import social_quest_service.entity.GuildMember;
import social_quest_service.entity.enums.GuildRole;
import social_quest_service.mapper.SocialQuestMapper;
import social_quest_service.repository.GuildMemberRepository;
import social_quest_service.repository.GuildRepository;
import social_quest_service.service.GuildService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuildServiceImpl implements GuildService {

    private final GuildRepository guildRepository;
    private final GuildMemberRepository guildMemberRepository;

    @Override
    @Transactional
    public GuildResponse createGuild(UUID leaderId, GuildCreateRequest request) {
        if (guildMemberRepository.findByPlayerId(leaderId).isPresent()) {
            throw new BusinessException(CommonErrorCode.CONFLICT, "Player is already a member of a guild");
        }

        if (guildRepository.existsByName(request.name())) {
            throw new BusinessException(CommonErrorCode.CONFLICT, "Guild name already exists: " + request.name());
        }

        Guild guild = Guild.builder()
                .name(request.name())
                .leaderId(leaderId)
                .level(1)
                .maxMembers(50)
                .description(request.description())
                .bannerAssetKey(request.bannerAssetKey())
                .createdAt(Instant.now())
                .build();
        guild = guildRepository.save(guild);

        GuildMember leaderMember = GuildMember.builder()
                .guildId(guild.getGuildId())
                .playerId(leaderId)
                .role(GuildRole.LEADER)
                .joinedAt(Instant.now())
                .build();
        guildMemberRepository.save(leaderMember);

        log.info("Player {} created guild {} ({})", leaderId, guild.getName(), guild.getGuildId());
        return SocialQuestMapper.toGuildResponse(guild, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public GuildResponse getGuild(UUID guildId) {
        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Guild not found"));
        long memberCount = guildMemberRepository.countByGuildId(guildId);
        return SocialQuestMapper.toGuildResponse(guild, memberCount);
    }

    @Override
    @Transactional(readOnly = true)
    public GuildResponse getMyGuild(UUID playerId) {
        GuildMember membership = guildMemberRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Player is not in any guild"));
        return getGuild(membership.getGuildId());
    }

    @Override
    @Transactional
    public GuildMemberResponse joinGuild(UUID playerId, UUID guildId) {
        if (guildMemberRepository.findByPlayerId(playerId).isPresent()) {
            throw new BusinessException(CommonErrorCode.CONFLICT, "Player is already a member of a guild");
        }

        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Guild not found"));

        long currentCount = guildMemberRepository.countByGuildId(guildId);
        if (currentCount >= guild.getMaxMembers()) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Guild has reached maximum member capacity");
        }

        GuildMember member = GuildMember.builder()
                .guildId(guildId)
                .playerId(playerId)
                .role(GuildRole.MEMBER)
                .joinedAt(Instant.now())
                .build();
        member = guildMemberRepository.save(member);
        log.info("Player {} joined guild {}", playerId, guildId);
        return SocialQuestMapper.toGuildMemberResponse(member);
    }

    @Override
    @Transactional
    public void leaveGuild(UUID playerId, UUID guildId) {
        GuildMember member = guildMemberRepository.findById(new GuildMember.GuildMemberId(guildId, playerId))
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Member not found in guild"));

        if (member.getRole() == GuildRole.LEADER) {
            long count = guildMemberRepository.countByGuildId(guildId);
            if (count > 1) {
                throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Leader cannot leave guild without transferring leadership first");
            } else {
                // Disband guild
                guildMemberRepository.delete(member);
                guildRepository.deleteById(guildId);
                log.info("Leader {} left and disbanded guild {}", playerId, guildId);
                return;
            }
        }

        guildMemberRepository.delete(member);
        log.info("Player {} left guild {}", playerId, guildId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuildMemberResponse> getGuildMembers(UUID guildId) {
        return guildMemberRepository.findByGuildId(guildId).stream()
                .map(SocialQuestMapper::toGuildMemberResponse)
                .toList();
    }
}
