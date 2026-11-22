package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "item_transactions", schema = "economy")
@Getter @Setter @NoArgsConstructor
public class ItemTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_id", nullable = false, updatable = false)
    private Long txId;

    @Column(name = "inventory_id", nullable = false)
    @NotNull
    private UUID inventoryId;

    @Column(name = "slot_id")
    private UUID slotId;

    @Column(name = "item_code", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String itemCode;

    @Column(name = "delta", nullable = false)
    @NotNull
    private Integer delta;

    @Column(name = "reason", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String reason;

    @Column(name = "ref_id")
    private UUID refId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}