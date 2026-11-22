package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player_profiles", schema = "identity")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerProfile {

    @Id
    @Column(name = "player_id", nullable = false, updatable = false)
    private UUID playerId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @NotNull
    @Min(1)
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @NotNull
    @Min(0)
    @Column(name = "xp", nullable = false)
    private Long xp = 0L;

    @Size(max = 50)
    @Column(name = "title_code", length = 50)
    private String titleCode;

    @Size(max = 2000)
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

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
