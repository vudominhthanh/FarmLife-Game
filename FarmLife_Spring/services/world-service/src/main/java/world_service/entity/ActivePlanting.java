package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import world_service.entity.enums.PlantingStatus;

@Entity
@Table(name="active_plantings", schema="world", indexes=@Index(name="idx_plantings_ready", columnList="ready_at"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivePlanting {

    @Id
    @OneToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="plot_id", nullable=false)
    @NotNull
    private Plot plot;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="crop_code", nullable=false)
    @NotNull
    private CropCatalog crop;

    @Column(name="current_stage", nullable=false)
    @NotNull
    @Min(0)
    private Short currentStage=0;

    @Column(name="planted_at", nullable=false)
    @NotNull
    private OffsetDateTime plantedAt;

    @Column(name="next_stage_at", nullable=false)
    @NotNull
    private OffsetDateTime nextStageAt;

    @Column(name="ready_at", nullable=false)
    @NotNull
    private OffsetDateTime readyAt;

    @Column(name="is_watered", nullable=false)
    private boolean watered=false;

    @Column(name="last_watered_at")
    private OffsetDateTime lastWateredAt;

    @Column(name="fertilizer_code", length=50)
    @Size(max=50)
    private String fertilizerCode;

    @Column(name="harvest_count", nullable=false)
    @NotNull
    @Min(0)
    private Short harvestCount=0;

    @Column(name="is_withered", nullable=false)
    private boolean withered=false;

    @Column(name="reservation_id")
    private UUID reservationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @NotNull
    private PlantingStatus status= PlantingStatus.PENDING_ITEMS;

    @Version
    @Column(nullable=false)
    private Long version=0L;

    @PrePersist
    void init(){if(plantedAt==null) plantedAt=OffsetDateTime.now();}

}