package analytics_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ab_assignments", schema = "analytics")
@IdClass(AbAssignment.AbAssignmentId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AbAssignment {

    @Id
    @Column(name = "experiment_id", nullable = false)
    @NotNull
    private UUID experimentId;

    @Id
    @Column(name = "player_id", nullable = false)
    @NotNull
    private UUID playerId;

    @Column(name = "variant", length = 30, nullable = false)
    @NotBlank @Size(max = 30)
    private String variant;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    public static class AbAssignmentId implements Serializable {
        private UUID experimentId;
        private UUID playerId;
    }
}