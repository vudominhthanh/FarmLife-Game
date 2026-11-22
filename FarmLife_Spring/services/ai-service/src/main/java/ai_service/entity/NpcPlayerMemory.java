package ai_service.entity;

import ai_service.entity.converter.PgVectorConverter;
import ai_service.entity.enums.Relationship;
import com.pgvector.PGvector;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "npc_player_memories", schema = "ai",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_npc_player",
                columnNames = {"npc_code", "player_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NpcPlayerMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "memory_id", nullable = false, updatable = false)
    private UUID memoryId;

    @Column(name = "npc_code", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String npcCode;

    @Column(name = "player_id", nullable = false)
    @NotNull
    private UUID playerId;

    @Column(name = "affection_score", nullable = false)
    @NotNull @Min(-100) @Max(1000)
    private Integer affectionScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", length = 30, nullable = false)
    @NotNull
    private Relationship relationship = Relationship.STRANGER;

    @Column(name = "memory_summary", nullable = false, columnDefinition = "text")
    @NotBlank
    private String memorySummary =
            "Mới chuyển đến nông trại, chưa có tương tác đáng chú ý.";

    @Column(name = "summary_updated_at", nullable = false)
    @NotNull
    private Instant summaryUpdatedAt = Instant.now();

    @Convert(converter = PgVectorConverter.class)
    @Column(name = "embedding", columnDefinition = "vector(768)")
    private PGvector embedding;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "last_interacted", nullable = false)
    @NotNull
    private Instant lastInteracted = Instant.now();
}