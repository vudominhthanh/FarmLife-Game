package social_quest_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gifts", schema = "social_quest")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Gift {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "gift_id", nullable = false, updatable = false)
    private UUID giftId;

    @Column(name = "sender_id", nullable = false)
    @NotNull
    private UUID senderId;

    @Column(name = "receiver_id", nullable = false)
    @NotNull
    private UUID receiverId;

    @Column(name = "item_code", length = 50)
    @Size(max = 50)
    private String itemCode;

    @Column(name = "quantity", nullable = false)
    @NotNull @Positive
    private Integer quantity = 1;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @Column(name = "claimed", nullable = false)
    @NotNull
    private Boolean claimed = false;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}