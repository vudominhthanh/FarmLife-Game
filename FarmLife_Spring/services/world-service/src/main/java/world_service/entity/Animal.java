package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import world_service.entity.enums.AnimalStage;

@Entity
@Table(
        name="animals",
        schema="world",
        indexes={@Index(name="idx_animals_player", columnList="player_id"),
                @Index(name="idx_animals_housing", columnList="housing_object_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Animal {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    @UuidGenerator
    @Column(name="animal_id", nullable=false, updatable=false)
    private UUID animalId;

    @Column(name="player_id", nullable=false)
    @NotNull
    private UUID playerId;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="housing_object_id", nullable=false)
    @NotNull
    private PlacedObject housing;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="animal_type_code", nullable=false)
    @NotNull
    private AnimalTypeCatalog animalType;

    @Column(length=50)
    @Size(max=50)
    private String name;

    @Column(name="age_sec", nullable=false)
    @NotNull
    @PositiveOrZero
    private Long ageSec=0L;

    @Column(nullable=false)
    @NotNull
    @Min(0)
    @Max(100)
    private Short hunger=100;

    @Column(nullable=false)
    @NotNull
    @Min(0)
    @Max(100)
    private Short happiness=100;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @NotNull
    private AnimalStage stage = AnimalStage.BABY;

    @Column(name="last_fed_at")
    private OffsetDateTime lastFedAt;

    @Column(name="last_product_at")
    private OffsetDateTime lastProductAt;

    @Column(name="feeding_reservation_id")
    private UUID feedingReservationId;

    @Version
    @Column(nullable=false)
    private Long version=0L;

    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @PrePersist
    void init(){if(createdAt==null) createdAt=OffsetDateTime.now();}
}