package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import world_service.entity.enums.OutboxStatus;

@Entity
@Table(
        name="outbox_events",
        schema="world",
        indexes=@Index(name="idx_outbox_pending", columnList="created_at")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    @UuidGenerator
    @Column(name="event_id", nullable=false, updatable=false)
    private UUID eventId;

    @Column(name="aggregate_type", nullable=false, length=50)
    @NotBlank
    @Size(max=50)
    private String aggregateType;

    @Column(name="aggregate_id", nullable=false, length=100)
    @NotBlank
    @Size(max=100)
    private String aggregateId;

    @Column(name="event_type", nullable=false, length=80)
    @NotBlank
    @Size(max=80)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable=false, columnDefinition="jsonb")
    @NotNull
    private java.util.Map<String,Object> payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @NotNull
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name="retry_count", nullable=false)
    @NotNull
    @Min(0)
    @Max(32767)
    private Short retryCount=0;

    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="published_at")
    private OffsetDateTime publishedAt;

    @PrePersist
    void init(){if(createdAt==null) createdAt=OffsetDateTime.now();}
}