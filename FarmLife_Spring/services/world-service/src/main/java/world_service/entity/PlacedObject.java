package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import world_service.entity.enums.ObjectType;

@Entity
@Table(
        name="placed_objects",
        schema="world",
        indexes=@Index(name="idx_objects_farm", columnList="farm_id, object_type")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacedObject {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    @UuidGenerator
    @Column(name="object_id", nullable=false, updatable=false)
    private UUID objectId;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="farm_id", nullable=false)
    @NotNull
    private Farm farm;

    @Column(name="object_code", nullable=false, length=50)
    @NotBlank
    @Size(max=50)
    private String objectCode;

    @Enumerated(EnumType.STRING)
    @Column(name="object_type", nullable=false, length=30)
    @NotNull
    private ObjectType objectType;

    @Column(name="origin_x", nullable=false)
    @NotNull
    private Integer originX;

    @Column(name="origin_y", nullable=false)
    @NotNull
    private Integer originY;

    @Column(nullable=false)
    @NotNull
    @Positive
    @Max(32767)
    private Short width=1;

    @Column(nullable=false)
    @NotNull
    @Positive
    @Max(32767)
    private Short height=1;

    @Column(nullable=false)
    @NotNull
    @Min(0)
    @Max(3)
    private Short direction=0;

    @Column(name="max_capacity")
    @Positive
    private Integer maxCapacity;

    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name="state_metadata", nullable=false, columnDefinition="jsonb")
    @NotNull
    private Map<String,Object> stateMetadata=new HashMap<>();

    @Version @Column(nullable=false)
    private Long version=0L;

    @Column(name="placed_at", nullable=false, updatable=false)
    private OffsetDateTime placedAt;

    @PrePersist
    void init(){if(placedAt==null) placedAt=OffsetDateTime.now();}
}