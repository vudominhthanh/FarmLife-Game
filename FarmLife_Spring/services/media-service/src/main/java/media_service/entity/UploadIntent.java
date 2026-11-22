package media_service.entity;

import media_service.entity.enums.UploadPurpose;
import media_service.entity.enums.UploadStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "upload_intents", schema = "media")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UploadIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "intent_id", nullable = false, updatable = false)
    private UUID intentId;

    @Column(name = "player_id", nullable = false)
    @NotNull
    private UUID playerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 30, nullable = false)
    @NotNull
    private UploadPurpose purpose;

    @Column(name = "expected_type", length = 80, nullable = false)
    @NotBlank @Size(max = 80)
    private String expectedType;

    @Column(name = "max_size_bytes", nullable = false)
    @NotNull @Positive
    private Long maxSizeBytes;

    @Column(name = "temp_object_key", length = 300, nullable = false)
    @NotBlank @Size(max = 300)
    private String tempObjectKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private UploadStatus status = UploadStatus.PENDING;

    @Column(name = "asset_id")
    private UUID assetId;

    @Column(name = "rejection_reason", length = 120)
    @Size(max = 120)
    private String rejectionReason;

    @Column(name = "expires_at", nullable = false)
    @NotNull
    private Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}