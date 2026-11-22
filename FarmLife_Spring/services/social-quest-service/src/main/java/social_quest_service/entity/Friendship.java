package social_quest_service.entity;

import social_quest_service.entity.enums.FriendshipStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friendships", schema = "social_quest")
@IdClass(Friendship.FriendshipId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Friendship {

    @Id
    @Column(name = "player_a_id", nullable = false)
    @NotNull
    private UUID playerAId;

    @Id
    @Column(name = "player_b_id", nullable = false)
    @NotNull
    private UUID playerBId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(name = "requested_by", nullable = false)
    @NotNull
    private UUID requestedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Lưu ý: DB có CHECK (player_a_id < player_b_id). Validation canonical order
    // được enforce ở DB (Postgres uuid so sánh unsigned byte, khác với
    // UUID.compareTo() của Java so sánh signed). Service phải canonicalize
    // (min, max) trước khi gọi save — không nên dựa vào @AssertTrue ở đây
    // vì có thể false-positive do khác biệt signed/unsigned.

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    public static class FriendshipId implements Serializable {
        private UUID playerAId;
        private UUID playerBId;
    }
}