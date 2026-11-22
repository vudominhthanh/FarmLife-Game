package ai_service.entity;

import ai_service.entity.enums.PregenStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "pregenerated_quests", schema = "ai")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PregeneratedQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pregen_id", nullable = false, updatable = false)
    private UUID pregenId;

    @Column(name = "player_id", nullable = false)
    @NotNull
    private UUID playerId;

    @Column(name = "npc_code", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String npcCode;

    @Column(name = "rule_id", nullable = false)
    @NotNull
    private Integer ruleId;

    @Column(name = "source_event_id")
    private UUID sourceEventId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quest_payload", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> questPayload;

    @Column(name = "dialogue_text", nullable = false, columnDefinition = "text")
    @NotBlank
    private String dialogueText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private PregenStatus status = PregenStatus.READY;

    @Column(name = "assigned_player_quest_id")
    private UUID assignedPlayerQuestId;

    @Column(name = "expires_at", nullable = false)
    @NotNull
    private Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}