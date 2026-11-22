package social_quest_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "guilds", schema = "social_quest")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Guild {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "guild_id", nullable = false, updatable = false)
    private UUID guildId;

    @Column(name = "name", length = 80, nullable = false, unique = true)
    @NotBlank @Size(max = 80)
    private String name;

    @Column(name = "leader_id", nullable = false)
    @NotNull
    private UUID leaderId;

    @Column(name = "level", nullable = false)
    @NotNull @Positive
    private Integer level = 1;

    @Column(name = "max_members", nullable = false, columnDefinition = "smallint")
    @NotNull @Positive
    private Integer maxMembers = 50;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "banner_asset_key", length = 300)
    @Size(max = 300)
    private String bannerAssetKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}