package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customizations", schema = "identity")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Customization {

    @Id
    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerProfile profile;

    @Size(max = 50)
    @Column(name = "avatar_code", length = 50)
    private String avatarCode;

    @Size(max = 300)
    @Column(name = "custom_avatar_asset_key", length = 300)
    private String customAvatarAssetKey;

    @Size(max = 50)
    @Column(name = "frame_code", length = 50)
    private String frameCode;

    @Size(max = 50)
    @Column(name = "farm_theme_code", length = 50)
    private String farmThemeCode;

    @Size(max = 20)
    @Column(name = "ui_palette", length = 20)
    private String uiPalette;

    @Size(max = 50)
    @Column(name = "pet_companion_code", length = 50)
    private String petCompanionCode;

    @Version
    @NotNull
    @Min(0)
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @AssertTrue(message = "avatarCode and customAvatarAssetKey cannot both be set")
    public boolean isAvatarConfigurationValid() {
        return avatarCode == null || customAvatarAssetKey == null;
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
