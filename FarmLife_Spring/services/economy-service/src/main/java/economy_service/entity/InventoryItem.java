package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "inventory_items",
        schema = "economy",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_inv_slot", columnNames = {"inventory_id", "slot_index"})
)
@Getter @Setter @NoArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slot_id", nullable = false, updatable = false)
    private UUID slotId;

    @Column(name = "inventory_id", nullable = false)
    @NotNull
    private UUID inventoryId;

    @Column(name = "slot_index", nullable = false, columnDefinition = "smallint")
    @NotNull @Min(0)
    private Integer slotIndex;

    @Column(name = "item_code", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String itemCode;

    @Column(name = "quantity", nullable = false)
    @NotNull @Positive
    private Integer quantity;

    @Column(name = "locked_quantity", nullable = false)
    @NotNull @PositiveOrZero
    private Integer lockedQuantity = 0;

    @Column(name = "quality", nullable = false, columnDefinition = "smallint")
    @NotNull @Min(0) @Max(100)
    private Integer quality = 100;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "instance_data", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> instanceData = new HashMap<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "acquired_at", nullable = false, updatable = false)
    private Instant acquiredAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @AssertTrue(message = "lockedQuantity must not exceed quantity")
    public boolean isLockedQuantityWithinLimit() {
        return lockedQuantity == null || quantity == null || lockedQuantity <= quantity;
    }
}