package entity;

import entity.enums.ItemCategory;
import entity.enums.ItemRarity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "items_catalog", schema = "economy")
@Getter @Setter @NoArgsConstructor
public class ItemCatalog {

    @Id
    @Column(name = "item_code", length = 50, nullable = false, updatable = false)
    @NotBlank @Size(max = 50)
    private String itemCode;

    @Column(name = "name", length = 100, nullable = false)
    @NotBlank @Size(max = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30, nullable = false)
    @NotNull
    private ItemCategory category;

    @Column(name = "stackable", nullable = false)
    @NotNull
    private Boolean stackable = true;

    @Column(name = "max_stack", nullable = false)
    @NotNull @Min(1)
    private Integer maxStack = 999;

    @Column(name = "base_value", nullable = false)
    @NotNull @PositiveOrZero
    private Long baseValue = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", length = 20, nullable = false)
    @NotNull
    private ItemRarity rarity = ItemRarity.COMMON;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> metadata = new HashMap<>();

    @Column(name = "icon_asset_key", length = 300)
    @Size(max = 300)
    private String iconAssetKey;

    @Column(name = "is_tradable", nullable = false)
    @NotNull
    private Boolean isTradable = true;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;
}