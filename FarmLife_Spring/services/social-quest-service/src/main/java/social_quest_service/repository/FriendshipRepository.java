package social_quest_service.repository;

import social_quest_service.entity.Friendship;
import social_quest_service.entity.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Friendship.FriendshipId> {

    @Query("SELECT f FROM Friendship f WHERE (f.playerAId = :playerId OR f.playerBId = :playerId) AND f.status = :status")
    List<Friendship> findAllByPlayerAndStatus(@Param("playerId") UUID playerId, @Param("status") FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE (f.playerAId = :playerId OR f.playerBId = :playerId)")
    List<Friendship> findAllByPlayer(@Param("playerId") UUID playerId);

    @Query("SELECT f FROM Friendship f WHERE (f.playerAId = :p1 AND f.playerBId = :p2) OR (f.playerAId = :p2 AND f.playerBId = :p1)")
    Optional<Friendship> findFriendshipBetween(@Param("p1") UUID p1, @Param("p2") UUID p2);
}
