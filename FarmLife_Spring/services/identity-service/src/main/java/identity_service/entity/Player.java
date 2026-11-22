package entity;

import entity.enums.PlayerStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "players",
        schema = "identity",
        indexes = {
                @Index(
                        name = "idx_players_region",
                        columnList = "region"
                )
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "player_id", nullable = false, updatable = false)
    private UUID playerId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            unique = true
    )
    private Account account;

    @NotBlank
    @Size(max = 50)
    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @NotBlank
    @Size(max = 10)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Column(name = "region", nullable = false, length = 10)
    private String region = "sea";

    @NotBlank
    @Size(max = 10)
    @Pattern(regexp = "^[a-zA-Z-]+$")
    @Column(name = "language", nullable = false, length = 10)
    private String language = "vi";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PlayerStatus status = PlayerStatus.ACTIVE;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
