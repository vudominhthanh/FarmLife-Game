package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="animal_types_catalog", schema="world")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnimalTypeCatalog {
    @Id
    @Column(name="animal_type_code", length=50)
    @NotBlank
    @Size(max=50)
    private String animalTypeCode;

    @Column(nullable=false, length=80)
    @NotBlank
    @Size(max=80)
    private String name;

    @Column(name="mature_sec", nullable=false)
    @NotNull
    @Positive
    private Integer matureSec;

    @Column(name="product_item_code", length=50)
    @Size(max=50)
    private String productItemCode;

    @Column(name="product_interval_sec")
    @Positive
    private Integer productIntervalSec;

    @Column(name="feed_item_code", length=50)
    @Size(max=50)
    private String feedItemCode;

    @Column(name="feed_amount_per_day")
    @Positive
    private Integer feedAmountPerDay=1;

    @Column(name="housing_required", nullable=false)
    private boolean housingRequired=true;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name="sprite_asset_keys", columnDefinition="text[]")
    private String[] spriteAssetKeys;

    @Column(name="is_active", nullable=false)
    private boolean active=true;
}