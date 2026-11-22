package entity;

import entity.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "market_orders", schema = "economy")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarketOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "listing_id", nullable = false)
    @NotNull
    private UUID listingId;

    @Column(name = "buyer_id", nullable = false)
    @NotNull
    private UUID buyerId;

    @Column(name = "quantity", nullable = false)
    @NotNull @Positive
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    @NotNull @Positive
    private Long unitPrice;

    @Column(name = "total_price", nullable = false)
    @NotNull @Positive
    private Long totalPrice;

    @Column(name = "tax", nullable = false)
    @NotNull @PositiveOrZero
    private Long tax = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private OrderStatus status = OrderStatus.COMPLETED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}