package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import world_service.entity.enums.PermissionRole;
import world_service.entity.id.FarmPermissionId;

@Entity
@Table(name="farm_permissions", schema="world")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmPermission {
    @EmbeddedId
    @NotNull
    private FarmPermissionId id;

    @MapsId("farmId")
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="farm_id", nullable=false)
    private Farm farm;

    @Column(nullable=false, length=20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private PermissionRole role = PermissionRole.VISITOR;

    @Column(name="can_water", nullable=false)
    private boolean canWater=true;

    @Column(name="can_harvest", nullable=false)
    private boolean canHarvest=false;

    @Column(name="can_build", nullable=false)
    private boolean canBuild=false;

    @Column(name="granted_at", nullable=false, updatable=false)
    private OffsetDateTime grantedAt;

    @PrePersist
    void init(){if(grantedAt==null) grantedAt=OffsetDateTime.now();}
}
