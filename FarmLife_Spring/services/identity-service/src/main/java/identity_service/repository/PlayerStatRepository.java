package repository;

import entity.PlayerStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface PlayerStatRepository extends JpaRepository<PlayerStat, UUID> {

    @Modifying
    @Query("UPDATE PlayerStat s SET s.totalHarvests = s.totalHarvests + :count, s.updatedAt = :now WHERE s.playerId = :playerId")
    int incrementHarvests(@Param("playerId") UUID playerId, @Param("count") long count, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE PlayerStat s SET s.totalTrades = s.totalTrades + :trades, s.totalCoinsEarned = s.totalCoinsEarned + :coins, s.updatedAt = :now WHERE s.playerId = :playerId")
    int incrementTradesAndCoins(@Param("playerId") UUID playerId, @Param("trades") long trades, @Param("coins") long coins, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE PlayerStat s SET s.totalAnimalsRaised = s.totalAnimalsRaised + :count, s.updatedAt = :now WHERE s.playerId = :playerId")
    int incrementAnimalsRaised(@Param("playerId") UUID playerId, @Param("count") long count, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE PlayerStat s SET s.lastActiveAt = :now, s.updatedAt = :now WHERE s.playerId = :playerId")
    int updateLastActive(@Param("playerId") UUID playerId, @Param("now") Instant now);
}
