package analytics_service.repository;

import analytics_service.entity.PlayerDailyMetric;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerDailyMetricRepository extends JpaRepository<PlayerDailyMetric, PlayerDailyMetric.PlayerDailyMetricId> {

    Optional<PlayerDailyMetric> findByPlayerIdAndDay(UUID playerId, LocalDate day);

    List<PlayerDailyMetric> findByPlayerIdOrderByDayDesc(UUID playerId, Pageable pageable);

    @Modifying
    @Query(value = """
        INSERT INTO analytics.player_daily_metrics (player_id, day, sessions, minutes_played, coins_earned, harvests, trades)
        VALUES (:playerId, :day, :sessions, :minutesPlayed, :coinsEarned, :harvests, :trades)
        ON CONFLICT (player_id, day) DO UPDATE SET
            sessions = analytics.player_daily_metrics.sessions + EXCLUDED.sessions,
            minutes_played = analytics.player_daily_metrics.minutes_played + EXCLUDED.minutes_played,
            coins_earned = analytics.player_daily_metrics.coins_earned + EXCLUDED.coins_earned,
            harvests = analytics.player_daily_metrics.harvests + EXCLUDED.harvests,
            trades = analytics.player_daily_metrics.trades + EXCLUDED.trades
        """, nativeQuery = true)
    int upsertIncrementMetrics(
            @Param("playerId") UUID playerId,
            @Param("day") LocalDate day,
            @Param("sessions") int sessions,
            @Param("minutesPlayed") int minutesPlayed,
            @Param("coinsEarned") long coinsEarned,
            @Param("harvests") int harvests,
            @Param("trades") int trades
    );
}
