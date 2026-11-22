package media_service.entity;

import media_service.entity.enums.VariantFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "asset_variants", schema = "media",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_variant_unique",
                        columnNames = {"asset_id", "variant_name", "format"})
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssetVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "variant_id", nullable = false, updatable = false)
    private UUID variantId;

    @Column(name = "asset_id", nullable = false)
    @NotNull
    private UUID assetId;

    @Column(name = "variant_name", length = 30, nullable = false)
    @NotBlank @Size(max = 30)
    private String variantName;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 10, nullable = false)
    @NotNull
    private VariantFormat format;

    @Column(name = "asset_key", length = 300, nullable = false, unique = true)
    @NotBlank @Size(max = 300)
    private String assetKey;

    // Lưu ý: schema KHÔNG có CHECK cho cột này (khác với assets.bucket).
    // Giữ String để phản ánh đúng DB (permissive), tránh fail khi đọc
    // giá trị ngoài enum. Nếu muốn type-safe hơn, đổi sang AssetBucket.
    @Column(name = "bucket", length = 60, nullable = false)
    @NotBlank @Size(max = 60)
    private String bucket;

    @Column(name = "width")
    @Positive
    private Integer width;

    @Column(name = "height")
    @Positive
    private Integer height;

    @Column(name = "size_bytes", nullable = false)
    @NotNull @Positive
    private Long sizeBytes;

    @Column(name = "sha256", length = 64, nullable = false)
    @NotBlank @Size(min = 64, max = 64)
    private String sha256;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}