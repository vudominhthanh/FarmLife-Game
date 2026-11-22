package analytics_service.entity;

import analytics_service.entity.enums.ExperimentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "ab_experiments", schema = "analytics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AbExperiment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "experiment_id", nullable = false, updatable = false)
    private UUID experimentId;

    @Column(name = "name", length = 80, nullable = false, unique = true)
    @NotBlank @Size(max = 80)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variants", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> variants;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    @Builder.Default
    private ExperimentStatus status = ExperimentStatus.DRAFT;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;
}