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
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys", schema = "economy")
@Getter @Setter @NoArgsConstructor
public class IdempotencyKey {

    @Id
    @Column(name = "key", nullable = false, updatable = false)
    @NotNull
    private UUID key;

    @Column(name = "player_id", nullable = false)
    @NotNull
    private UUID playerId;

    @Column(name = "endpoint", length = 120, nullable = false)
    @NotBlank @Size(max = 120)
    private String endpoint;

    @Column(name = "request_hash", nullable = false, columnDefinition = "text")
    @NotBlank
    private String requestHash;

    @Column(name = "response_status", columnDefinition = "smallint")
    private Integer responseStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private Map<String, Object> responseBody;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    @NotNull
    private Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);
}