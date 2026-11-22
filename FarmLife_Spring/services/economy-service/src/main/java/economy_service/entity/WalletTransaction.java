package entity;

import entity.enums.CurrencyType;
import entity.enums.WalletTransactionType;
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
@Table(name = "transactions", schema = "economy")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tx_id", nullable = false, updatable = false)
    private UUID txId;

    @Column(name = "wallet_id", nullable = false)
    @NotNull
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", length = 10, nullable = false)
    @NotNull
    private CurrencyType currency;

    @Column(name = "amount", nullable = false)
    @NotNull
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    @NotNull
    private WalletTransactionType type;

    @Column(name = "ref_type", length = 30)
    @Size(max = 30)
    private String refType;

    @Column(name = "ref_id")
    private UUID refId;

    @Column(name = "balance_after", nullable = false)
    @NotNull @PositiveOrZero
    private Long balanceAfter;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}