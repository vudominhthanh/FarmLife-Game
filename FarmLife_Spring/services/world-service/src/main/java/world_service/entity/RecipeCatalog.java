package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name="recipes_catalog", schema="world")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeCatalog {
    @Id
    @Column(name="recipe_code", length=50)
    @NotBlank
    @Size(max=50)
    private String recipeCode;

    @Column(name="output_item_code", length=50, nullable=false)
    @NotBlank
    @Size(max=50)
    private String outputItemCode;

    @Column(name="output_qty", nullable=false)
    @NotNull
    @Positive
    private Integer outputQty=1;

    @Column(name="craft_time_sec", nullable=false)
    @NotNull
    @PositiveOrZero
    private Integer craftTimeSec;

    @Column(name="required_level", nullable=false)
    @NotNull
    @PositiveOrZero
    private Integer requiredLevel=1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable=false, columnDefinition="jsonb")
    @NotNull
    private Map<String,Object> ingredients;

    @Column(name="machine_type", length=50)
    @Size(max=50)
    private String machineType;

    @Column(name="icon_asset_key", length=300)
    @Size(max=300)
    private String iconAssetKey;

    @Column(name="is_active", nullable=false)
    private boolean active=true;
}