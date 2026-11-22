package entity;

import entity.enums.EscrowReason;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "escrow_records", schema = "economy")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EscrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "escrow_id", nullable = false, updatable = false)
    private UUID escrowId;

    @Column(name = "player_id", nullable = false)
    @NotNull
    private UUID playerId;

    @Column(name = "item_code", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String itemCode;

    @Column(name = "quantity", nullable = false)
    @NotNull @Positive
    private Integer quantity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "slots", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> slots;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 30, nullable = false)
    @NotNull
    private EscrowReason reason;

    @Column(name = "ref_id", nullable = false)
    @NotNull
    private UUID refId;

    @Column(name = "released_at")
    private Instant releasedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}