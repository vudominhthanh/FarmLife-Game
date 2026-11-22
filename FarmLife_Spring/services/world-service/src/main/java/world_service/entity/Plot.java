package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name="plots",
        schema="world",
        uniqueConstraints=@UniqueConstraint(name="uk_plot_farm_coord", columnNames={"farm_id","coord_x","coord_y"}),
        indexes=@Index(name="idx_plots_farm", columnList="farm_id")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plot {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    @UuidGenerator
    @Column(name="plot_id", nullable=false, updatable=false)
    private UUID plotId;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="farm_id", nullable=false)
    @NotNull
    private Farm farm;

    @Column(name="coord_x", nullable=false)
    @NotNull
    private Short coordX;

    @Column(name="coord_y", nullable=false)
    @NotNull
    private Short coordY;

    @Column(name="soil_quality", nullable=false)
    @NotNull
    @Min(0)
    @Max(100)
    private Short soilQuality=100;

    @Column(name="terrain_type", nullable=false, length=20)
    @NotBlank
    @Size(max=20)
    private String terrainType="soil";

    @Column(name="is_locked", nullable=false)
    private boolean locked=false;
}
