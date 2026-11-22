package entity;

import tools.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player_settings", schema = "identity")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerSetting {

    @Id
    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerProfile profile;

    @NotBlank
    @Size(max = 10)
    @Pattern(regexp = "^[a-zA-Z-]+$")
    @Column(name = "language", nullable = false, length = 10)
    private String language = "vi";

    @NotNull
    @Min(0)
    @Max(100)
    @Column(name = "music_volume", nullable = false)
    private Short musicVolume = 80;

    @NotNull
    @Min(0)
    @Max(100)
    @Column(name = "sfx_volume", nullable = false)
    private Short sfxVolume = 100;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notifications", nullable = false, columnDefinition = "jsonb")
    private JsonNode notifications;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "privacy", nullable = false, columnDefinition = "jsonb")
    private JsonNode privacy;

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
