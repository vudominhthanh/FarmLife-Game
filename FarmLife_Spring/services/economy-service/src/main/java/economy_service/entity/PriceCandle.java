package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "price_candles", schema = "economy")
@IdClass(PriceCandle.PriceCandleId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceCandle {

    @Id
    @Column(name = "item_code", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String itemCode;

    @Id
    @Column(name = "bucket_time", nullable = false)
    @NotNull
    private Instant bucketTime;

    @Column(name = "open_price", nullable = false)
    @NotNull @Positive
    private Long openPrice;

    @Column(name = "high_price", nullable = false)
    @NotNull @Positive
    private Long highPrice;

    @Column(name = "low_price", nullable = false)
    @NotNull @Positive
    private Long lowPrice;

    @Column(name = "close_price", nullable = false)
    @NotNull @Positive
    private Long closePrice;

    @Column(name = "volume_traded", nullable = false)
    @NotNull @PositiveOrZero
    private Long volumeTraded = 0L;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    public static class PriceCandleId implements Serializable {
        private String itemCode;
        private Instant bucketTime;
    }
}