package entity;

import entity.enums.PlayerStyleTag;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "playstyles",
        schema = "identity",
        indexes = {
                @Index(name = "idx_playstyles_ai", columnList = "ai_profiled_at")
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Playstyle {

    @Id
    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerProfile profile;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(
            name = "preferred_crop_codes",
            columnDefinition = "text[]"
    )
    private String[] preferredCropCodes = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(
            name = "preferred_animal_codes",
            columnDefinition = "text[]"
    )
    private String[] preferredAnimalCodes = new String[0];

    @Enumerated(EnumType.STRING)
    @Column(name = "play_style_tag", length = 30)
    private PlayerStyleTag playStyleTag;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> tags;

    @Column(name = "ai_profiled_at")
    private Instant aiProfiledAt;

    @Version
    @NotNull
    @Min(0)
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
