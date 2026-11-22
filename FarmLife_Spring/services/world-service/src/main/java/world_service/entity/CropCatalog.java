package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name="crops_catalog", schema="world")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropCatalog {
    @Id
    @Column(name="crop_code", length=50, nullable=false)
    @NotBlank
    @Size(max=50)
    private String cropCode;

    @Column(name="seed_item_code", length=50, nullable=false)
    @NotBlank
    @Size(max=50)
    String seedItemCode;

    @Column(name="harvest_item_code", length=50, nullable=false)
    @NotBlank
    @Size(max=50)
    private String harvestItemCode;

    @Column(nullable=false, length=80)
    @NotBlank
    @Size(max=80)
    private String name;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name="stage_durations", columnDefinition="integer[]", nullable=false)
    @NotNull
    @Size(min=1)
    private Integer[] stageDurations;

    @Column(name="regrow_sec")
    @Positive
    private Integer regrowSec;

    @Column(name="min_yield", nullable=false)
    @NotNull
    @Positive
    private Integer minYield=1;

    @Column(name="max_yield", nullable=false)
    @NotNull
    @Positive
    private Integer maxYield=1;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name="allowed_seasons", columnDefinition="varchar(20)[]")
    private String[] allowedSeasons={"spring","summer","autumn"};

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name="sprite_asset_keys", columnDefinition="text[]")
    private String[] spriteAssetKeys;

    @Column(name="is_active", nullable=false)
    private boolean active=true;
}