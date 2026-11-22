package analytics_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "player_daily_metrics", schema = "analytics")
@IdClass(PlayerDailyMetric.PlayerDailyMetricId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlayerDailyMetric {

    @Id
    @Column(name = "player_id", nullable = false)
    @NotNull
    private UUID playerId;

    @Id
    @Column(name = "day", nullable = false)
    @NotNull
    private LocalDate day;

    @Column(name = "sessions", nullable = false)
    @NotNull @PositiveOrZero
    @Builder.Default
    private Integer sessions = 0;

    @Column(name = "minutes_played", nullable = false)
    @NotNull @PositiveOrZero
    @Builder.Default
    private Integer minutesPlayed = 0;

    @Column(name = "coins_earned", nullable = false)
    @NotNull @PositiveOrZero
    @Builder.Default
    private Long coinsEarned = 0L;

    @Column(name = "harvests", nullable = false)
    @NotNull @PositiveOrZero
    @Builder.Default
    private Integer harvests = 0;

    @Column(name = "trades", nullable = false)
    @NotNull @PositiveOrZero
    @Builder.Default
    private Integer trades = 0;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    public static class PlayerDailyMetricId implements Serializable {
        private UUID playerId;
        private LocalDate day;
    }
}