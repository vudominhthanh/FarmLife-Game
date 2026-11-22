package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets", schema = "economy")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;

    @Column(name = "player_id", nullable = false, unique = true)
    @NotNull
    private UUID playerId;

    @Column(name = "coins", nullable = false)
    @NotNull @PositiveOrZero
    private Long coins = 0L;

    @Column(name = "gems", nullable = false)
    @NotNull @PositiveOrZero
    private Long gems = 0L;

    @Column(name = "locked_coins", nullable = false)
    @NotNull @PositiveOrZero
    private Long lockedCoins = 0L;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @AssertTrue(message = "lockedCoins must not exceed coins")
    public boolean isLockedCoinsWithinLimit() {
        return lockedCoins == null || coins == null || lockedCoins <= coins;
    }
}