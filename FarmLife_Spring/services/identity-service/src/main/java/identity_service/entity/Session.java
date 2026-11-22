package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "sessions",
        schema = "identity",
        indexes = {
                @Index(
                        name = "idx_sessions_active",
                        columnList = "player_id, expires_at"
                ),
                @Index(
                        name = "idx_sessions_token",
                        columnList = "access_token_hash",
                        unique = true
                )
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id", updatable = false, nullable = false)
    private UUID sessionId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @NotBlank
    @Column(name = "access_token_hash", nullable = false)
    private String accessTokenHash;

    @NotBlank
    @Column(name = "refresh_token_hash", nullable = false)
    private String refreshTokenHash;

    @Size(max = 100)
    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "ip_address", columnDefinition = "inet")
    private InetAddress ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
