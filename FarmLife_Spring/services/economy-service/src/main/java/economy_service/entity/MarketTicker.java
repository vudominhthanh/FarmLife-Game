package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "market_tickers", schema = "economy")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarketTicker {

    @Id
    @Column(name = "item_code", length = 50, nullable = false, updatable = false)
    @NotBlank @Size(max = 50)
    private String itemCode;

    @Column(name = "base_price", nullable = false)
    @NotNull @Positive
    private Long basePrice;

    @Column(name = "min_price", nullable = false)
    @NotNull @PositiveOrZero
    private Long minPrice;

    @Column(name = "max_price", nullable = false)
    @NotNull @Positive
    private Long maxPrice;

    @Column(name = "current_price", nullable = false)
    @NotNull @Positive
    private Long currentPrice;

    @Column(name = "supply_volume_24h", nullable = false)
    @NotNull @PositiveOrZero
    private Long supplyVolume24h = 0L;

    @Column(name = "demand_volume_24h", nullable = false)
    @NotNull @PositiveOrZero
    private Long demandVolume24h = 0L;

    @Column(name = "volatility_factor", nullable = false, precision = 5, scale = 4)
    @NotNull
    @DecimalMin("0.0000") @DecimalMax("9.9999")
    @Digits(integer = 1, fraction = 4)
    private BigDecimal volatilityFactor = new BigDecimal("0.0500");

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_state", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> aiState = new HashMap<>();

    @Column(name = "last_ai_trained_at")
    private Instant lastAiTrainedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}