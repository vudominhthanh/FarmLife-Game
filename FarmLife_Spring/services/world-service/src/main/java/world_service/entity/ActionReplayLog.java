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
import world_service.entity.enums.ReplayStatus;
import world_service.entity.id.ActionReplayLogId;

@Entity
@Table(name="action_replay_log", schema="world")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionReplayLog {
    @EmbeddedId
    @NotNull
    private ActionReplayLogId id;

    @Column(name="action_type", nullable=false, length=50)
    @NotBlank
    @Size(max=50)
    private String actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable=false, columnDefinition="jsonb")
    @NotNull
    private Map<String,Object> payload;

    @Column(name="idempotency_key", nullable=false)
    @NotNull
    private UUID idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @NotNull
    private ReplayStatus status= ReplayStatus.APPLIED;

    @Column(name="rejection_reason", length=120)
    @Size(max=120)
    private String rejectionReason;

    @Column(name="applied_at", nullable=false)
    private OffsetDateTime appliedAt;

    @PrePersist
    void init(){if(appliedAt==null) appliedAt=OffsetDateTime.now();}
}
