package ai_service.entity;

import ai_service.entity.enums.FsmState;
import ai_service.entity.enums.NpcRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "npc_agents", schema = "ai")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NpcAgent {

    @Id
    @Column(name = "npc_code", length = 50, nullable = false, updatable = false)
    @NotBlank @Size(max = 50)
    private String npcCode;

    @Column(name = "display_name", length = 80, nullable = false)
    @NotBlank @Size(max = 80)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 40, nullable = false)
    @NotNull
    private NpcRole role;

    @Column(name = "persona_prompt", nullable = false, columnDefinition = "text")
    @NotBlank
    private String personaPrompt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "favorite_items", columnDefinition = "text[]")
    private List<String> favoriteItems = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "disliked_items", columnDefinition = "text[]")
    private List<String> dislikedItems = new ArrayList<>();

    @Column(name = "llm_model", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String llmModel = "qwen2.5:7b";

    @Enumerated(EnumType.STRING)
    @Column(name = "fsm_state", length = 30, nullable = false)
    @NotNull
    private FsmState fsmState = FsmState.IDLE;

    @Column(name = "current_map_code", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String currentMapCode = "town_square";

    @Column(name = "coord_x", nullable = false, columnDefinition = "smallint")
    @NotNull
    private Integer coordX = 0;

    @Column(name = "coord_y", nullable = false, columnDefinition = "smallint")
    @NotNull
    private Integer coordY = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schedule_json", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> scheduleJson = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "q_table_state", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> qTableState = new HashMap<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;

    @Column(name = "last_state_change_at", nullable = false)
    @NotNull
    private Instant lastStateChangeAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}