package media_service.entity;

import media_service.entity.enums.PurgeReason;
import media_service.entity.enums.PurgeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Bảng gốc là "cdn_purge_queue". Đặt tên class là CdnPurgeTask
 * (thay vì CdnPurgeQueueItem) cho ngắn gọn và rõ nghĩa "1 task purge".
 */
@Entity
@Table(name = "cdn_purge_queue", schema = "media")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CdnPurgeTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purge_id", nullable = false, updatable = false)
    private Long purgeId;

    @Column(name = "asset_key", length = 300, nullable = false)
    @NotBlank @Size(max = 300)
    private String assetKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 30, nullable = false)
    @NotNull
    private PurgeReason reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private PurgeStatus status = PurgeStatus.PENDING;

    @Column(name = "attempts", nullable = false, columnDefinition = "smallint")
    @NotNull @PositiveOrZero
    private Integer attempts = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;
}