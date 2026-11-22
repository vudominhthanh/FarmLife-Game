package entity;

import entity.enums.ReservationReason;
import entity.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "item_reservations", schema = "economy")
@Getter @Setter @NoArgsConstructor
public class ItemReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reservation_id", nullable = false, updatable = false)
    private UUID reservationId;

    @Column(name = "player_id", nullable = false)
    @NotNull
    private UUID playerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 30, nullable = false)
    @NotNull
    private ReservationReason reason;

    @Column(name = "ref_id", nullable = false)
    @NotNull
    private UUID refId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    @NotNull
    private Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}