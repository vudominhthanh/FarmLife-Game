package media_service.entity;

import media_service.entity.enums.AssetBucket;
import media_service.entity.enums.AssetStatus;
import media_service.entity.enums.AssetType;
import media_service.entity.enums.AssetVisibility;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assets", schema = "media")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "asset_id", nullable = false, updatable = false)
    private UUID assetId;

    @Column(name = "asset_key", length = 300, nullable = false, unique = true)
    @NotBlank @Size(max = 300)
    private String assetKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "bucket", length = 60, nullable = false)
    @NotNull
    private AssetBucket bucket;

    @Column(name = "sha256", length = 64, nullable = false)
    @NotBlank @Size(min = 64, max = 64)
    private String sha256;

    @Column(name = "content_type", length = 80, nullable = false)
    @NotBlank @Size(max = 80)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    @NotNull @Positive
    private Long sizeBytes;

    @Column(name = "width")
    @Positive
    private Integer width;

    @Column(name = "height")
    @Positive
    private Integer height;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", length = 20, nullable = false)
    @NotNull
    private AssetType assetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 20, nullable = false)
    @NotNull
    private AssetVisibility visibility = AssetVisibility.PUBLIC;

    @Column(name = "owner_service", length = 30)
    @Size(max = 30)
    private String ownerService;

    @Column(name = "owner_ref", length = 200)
    @Size(max = 200)
    private String ownerRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private AssetStatus status = AssetStatus.PENDING;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @Column(name = "deprecated_at")
    private Instant deprecatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}