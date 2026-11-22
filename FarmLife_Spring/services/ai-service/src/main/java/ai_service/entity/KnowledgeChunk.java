package ai_service.entity;

import ai_service.entity.converter.PgVectorConverter;
import ai_service.entity.enums.DocCategory;
import com.pgvector.PGvector;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "knowledge_chunks", schema = "ai",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_chunk_unique",
                columnNames = {"doc_category", "ref_code", "chunk_index"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "chunk_id", nullable = false, updatable = false)
    private UUID chunkId;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_category", length = 40, nullable = false)
    @NotNull
    private DocCategory docCategory;

    @Column(name = "ref_code", length = 50)
    @Size(max = 50)
    private String refCode;

    @Column(name = "chunk_index", nullable = false, columnDefinition = "smallint")
    @NotNull @PositiveOrZero
    private Integer chunkIndex = 0;

    @Column(name = "title", length = 150, nullable = false)
    @NotBlank @Size(max = 150)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    @NotBlank
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> metadata = new HashMap<>();

    @Column(name = "content_hash", length = 64, nullable = false)
    @NotBlank @Size(min = 64, max = 64)
    private String contentHash;

    @Column(name = "token_count", nullable = false)
    @NotNull @PositiveOrZero
    private Integer tokenCount = 0;

    @Convert(converter = PgVectorConverter.class)
    @Column(name = "embedding", nullable = false, columnDefinition = "vector(768)")
    @NotNull
    private PGvector embedding;

    @Column(name = "last_embedded_at", nullable = false)
    @NotNull
    private Instant lastEmbeddedAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}