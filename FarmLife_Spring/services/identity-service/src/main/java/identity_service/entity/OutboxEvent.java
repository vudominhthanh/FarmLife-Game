package entity;

import tools.jackson.databind.JsonNode;
import entity.enums.OutboxStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        schema = "identity",
        indexes = {
                @Index(name = "idx_outbox_pending", columnList = "created_at")
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @NotBlank
    @Size(max = 100)
    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @NotBlank
    @Size(max = 80)
    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.PENDING;

    @NotNull
    @Min(0)
    @Max(32767)
    @Column(name = "retry_count", nullable = false)
    private Short retryCount = 0;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @AssertTrue(message = "PUBLISHED event must have publishedAt")
    public boolean isPublishedTimeValid() {
        return status != OutboxStatus.PUBLISHED || publishedAt != null;
    }
}
