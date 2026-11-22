package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name="idempotency_keys",
        schema="world",
        indexes=@Index(name="idx_idem_expires", columnList="expires_at")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKey {
    @Id
    @Column(name="key", nullable=false, updatable=false)
    private UUID key;

    @Column(name="player_id", nullable=false)
    @NotNull
    private UUID playerId;

    @Column(nullable=false, length=120)
    @NotBlank
    @Size(max=120)
    private String endpoint;

    @Column(name="request_hash", nullable=false, columnDefinition="text")
    @NotBlank
    private String requestHash;

    @Column(name="response_status")
    @Min(100)
    @Max(599)
    private Short responseStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="response_body", columnDefinition="jsonb")
    private Map<String,Object> responseBody;

    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="expires_at", nullable=false)
    @NotNull
    private OffsetDateTime expiresAt;

    @PrePersist
    void init(){var now=OffsetDateTime.now(); if(createdAt==null) createdAt=now; if(expiresAt==null) expiresAt=now.plusHours(24);}
}