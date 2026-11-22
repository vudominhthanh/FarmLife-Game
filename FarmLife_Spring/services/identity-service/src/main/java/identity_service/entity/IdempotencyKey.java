package entity;

import tools.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "idempotency_keys",
        schema = "identity",
        indexes = {
                @Index(name = "idx_idem_expires", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class IdempotencyKey {

    @Id
    @NotNull
    @Column(name = "key", nullable = false, updatable = false)
    private UUID key;

    @NotNull
    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @NotBlank
    @Size(max = 120)
    @Column(name = "endpoint", nullable = false, length = 120)
    private String endpoint;

    @NotBlank
    @Column(name = "request_hash", nullable = false, columnDefinition = "TEXT")
    private String requestHash;

    @Min(100)
    @Max(599)
    @Column(name = "response_status")
    private Short responseStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private JsonNode responseBody;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @AssertTrue(message = "expiresAt must be later than createdAt")
    public boolean isExpirationValid() {
        return createdAt == null || expiresAt == null
                || expiresAt.isAfter(createdAt);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (expiresAt == null) {
            expiresAt = createdAt.plusSeconds(86400);
        }
    }
}
