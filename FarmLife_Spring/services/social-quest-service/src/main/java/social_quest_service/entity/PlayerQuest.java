package social_quest_service.entity;

import social_quest_service.entity.enums.PlayerQuestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "player_quests", schema = "social_quest",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_pq_assignment",
                columnNames = {"player_id", "quest_code", "assigned_at"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "player_quest_id", nullable = false, updatable = false)
    private UUID playerQuestId;

    @Column(name = "player_id", nullable = false)
    @NotNull
    private UUID playerId;

    @Column(name = "quest_code", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String questCode;

    @Column(name = "progress", nullable = false)
    @NotNull @PositiveOrZero
    private Integer progress = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private PlayerQuestStatus status = PlayerQuestStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}