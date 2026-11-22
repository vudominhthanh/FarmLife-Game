package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import world_service.entity.enums.Season;
import world_service.entity.enums.Weather;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="farms", schema="world")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Farm {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    @UuidGenerator
    @Column(name="farm_id", nullable=false, updatable=false)
    private UUID farmId;

    @Column(name="player_id", nullable=false, unique=true)
    @NotNull
    private UUID playerId;

    @Column(nullable=false, length=80)
    @NotBlank
    @Size(max=80)
    private String name="My Farm";

    @Column(name="size_level", nullable=false)
    @NotNull
    @Min(1)
    private Integer sizeLevel=1;

    @Column(name="grid_width", nullable=false)
    @NotNull
    @Min(1)
    @Max(32767)
    private Short gridWidth=20;

    @Column(name="grid_height", nullable=false)
    @NotNull
    @Min(1)
    @Max(32767)
    private Short gridHeight=20;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @NotNull
    private Weather weather = Weather.SUNNY;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @NotNull
    private Season season = Season.SPRING;

    @Version
    @Column(nullable=false)
    private Long version=0L;

    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate(){var now = OffsetDateTime.now(); if(createdAt==null) createdAt=now; updatedAt=now;}

    @PreUpdate
    void onUpdate(){updatedAt = OffsetDateTime.now();}
}