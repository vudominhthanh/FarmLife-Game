package entity;

import entity.enums.ListingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "market_listings", schema = "economy")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarketListing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "listing_id", nullable = false, updatable = false)
    private UUID listingId;

    @Column(name = "seller_id", nullable = false)
    @NotNull
    private UUID sellerId;

    @Column(name = "item_code", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String itemCode;

    @Column(name = "quality", nullable = false, columnDefinition = "smallint")
    @NotNull @Min(0) @Max(100)
    private Integer quality = 100;

    @Column(name = "initial_quantity", nullable = false)
    @NotNull @Positive
    private Integer initialQuantity;

    @Column(name = "remaining_quantity", nullable = false)
    @NotNull @PositiveOrZero
    private Integer remainingQuantity;

    @Column(name = "unit_price", nullable = false)
    @NotNull @Positive
    private Long unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 25, nullable = false)
    @NotNull
    private ListingStatus status = ListingStatus.PENDING_ESCROW;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "expires_at", nullable = false)
    @NotNull
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @AssertTrue(message = "remainingQuantity must not exceed initialQuantity")
    public boolean isRemainingQuantityWithinLimit() {
        return remainingQuantity == null || initialQuantity == null
                || remainingQuantity <= initialQuantity;
    }
}