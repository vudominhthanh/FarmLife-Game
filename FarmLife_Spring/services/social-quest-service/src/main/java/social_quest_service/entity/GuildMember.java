package social_quest_service.entity;

import social_quest_service.entity.enums.GuildRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "guild_members", schema = "social_quest")
@IdClass(GuildMember.GuildMemberId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GuildMember {

    @Id
    @Column(name = "guild_id", nullable = false)
    @NotNull
    private UUID guildId;

    @Id
    @Column(name = "player_id", nullable = false)
    @NotNull
    private UUID playerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    @NotNull
    private GuildRole role = GuildRole.MEMBER;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    public static class GuildMemberId implements Serializable {
        private UUID guildId;
        private UUID playerId;
    }
}