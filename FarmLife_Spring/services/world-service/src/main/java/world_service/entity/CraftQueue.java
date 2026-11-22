package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import world_service.entity.enums.CraftStatus;

@Entity
@Table(
        name="craft_queue",
        schema="world",
        indexes={@Index(name="idx_craft_machine", columnList="machine_object_id,status")
                ,@Index(name="idx_craft_player", columnList="player_id,status")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CraftQueue {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    @UuidGenerator
    @Column(name="queue_id", nullable=false, updatable=false)
    private UUID queueId;

    @Column(name="player_id", nullable=false)
    @NotNull
    private UUID playerId;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="machine_object_id", nullable=false)
    @NotNull
    private PlacedObject machine;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="recipe_code", nullable=false)
    @NotNull
    private RecipeCatalog recipe;

    @Column(nullable=false)
    @NotNull
    @Positive
    private Integer quantity=1;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20) @NotNull
    private CraftStatus status = CraftStatus.PENDING_ITEMS;

    @Column(name="reservation_id")
    private UUID reservationId;

    @Column(name="started_at")
    private OffsetDateTime startedAt;

    @Column(name="finishes_at")
    private OffsetDateTime finishesAt;

    @Version
    @Column(nullable=false)
    private Long version=0L;

    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @PrePersist
    void init(){if(createdAt==null) createdAt=OffsetDateTime.now();}
}