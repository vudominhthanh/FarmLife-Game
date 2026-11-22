package social_quest_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player_achievements", schema = "social_quest")
@IdClass(PlayerAchievement.PlayerAchievementId.class)
@Getter @Setter @NoArgsConstructor
public class PlayerAchievement {

    @Id
    @Column(name = "player_id", nullable = false)
    @NotNull
    private UUID playerId;

    @Id
    @Column(name = "achievement_code", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String achievementCode;

    @CreationTimestamp
    @Column(name = "unlocked_at", nullable = false, updatable = false)
    private Instant unlockedAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    public static class PlayerAchievementId implements Serializable {
        private UUID playerId;
        private String achievementCode;
    }
}