package entity;

import entity.enums.OutboxStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", schema = "economy")
@Getter @Setter @NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "aggregate_type", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 100, nullable = false)
    @NotBlank @Size(max = 100)
    private String aggregateId;

    @Column(name = "event_type", length = 80, nullable = false)
    @NotBlank @Size(max = 80)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "retry_count", nullable = false, columnDefinition = "smallint")
    @NotNull @PositiveOrZero
    private Integer retryCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;
}