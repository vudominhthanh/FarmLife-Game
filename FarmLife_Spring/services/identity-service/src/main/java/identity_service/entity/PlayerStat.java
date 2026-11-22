package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "player_stats",
        schema = "identity",
        indexes = {
                @Index(name = "idx_stats_active", columnList = "last_active_at")
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStat {

    @Id
    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerProfile profile;

    @NotNull
    @Min(0)
    @Column(name = "total_harvests", nullable = false)
    private Long totalHarvests = 0L;

    @NotNull
    @Min(0)
    @Column(name = "total_animals_raised", nullable = false)
    private Long totalAnimalsRaised = 0L;

    @NotNull
    @Min(0)
    @Column(name = "total_coins_earned", nullable = false)
    private Long totalCoinsEarned = 0L;

    @NotNull
    @Min(0)
    @Column(name = "total_trades", nullable = false)
    private Long totalTrades = 0L;

    @NotNull
    @Min(0)
    @Column(name = "friend_count", nullable = false)
    private Integer friendCount = 0;

    @NotNull
    @Min(0)
    @Column(name = "playtime_minutes", nullable = false)
    private Long playtimeMinutes = 0L;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
