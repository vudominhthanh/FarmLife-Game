package entity;

import entity.enums.MailboxSourceType;
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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "mailbox_items", schema = "economy")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MailboxItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "mail_item_id", nullable = false, updatable = false)
    private UUID mailItemId;

    @Column(name = "player_id", nullable = false)
    @NotNull
    private UUID playerId;

    @Column(name = "item_code", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String itemCode;

    @Column(name = "quantity", nullable = false)
    @NotNull @Positive
    private Integer quantity;

    @Column(name = "quality", nullable = false, columnDefinition = "smallint")
    @NotNull @Min(0) @Max(100)
    private Integer quality = 100;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "instance_data", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> instanceData = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 30, nullable = false)
    @NotNull
    private MailboxSourceType sourceType;

    @Column(name = "ref_id")
    private UUID refId;

    @Column(name = "claimed", nullable = false)
    @NotNull
    private Boolean claimed = false;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "expires_at", nullable = false)
    @NotNull
    private Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}