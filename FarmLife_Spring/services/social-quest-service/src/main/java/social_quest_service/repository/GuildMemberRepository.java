package social_quest_service.repository;

import social_quest_service.entity.GuildMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuildMemberRepository extends JpaRepository<GuildMember, GuildMember.GuildMemberId> {
    List<GuildMember> findByGuildId(UUID guildId);
    Optional<GuildMember> findByPlayerId(UUID playerId);
    long countByGuildId(UUID guildId);
    boolean existsByGuildIdAndPlayerId(UUID guildId, UUID playerId);
}
