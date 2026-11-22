package social_quest_service.entity;

import social_quest_service.entity.enums.QuestSource;
import social_quest_service.entity.enums.QuestType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "quests", schema = "social_quest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quest {

    @Id
    @Column(name = "quest_code", length = 50, nullable = false, updatable = false)
    @NotBlank @Size(max = 50)
    private String questCode;

    @Column(name = "title", length = 120, nullable = false)
    @NotBlank @Size(max = 120)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    @NotNull
    private QuestType type;

    @Column(name = "goal_type", length = 30, nullable = false)
    @NotBlank @Size(max = 30)
    private String goalType;

    @Column(name = "goal_target", nullable = false)
    @NotNull @Positive
    private Integer goalTarget;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reward_json", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> rewardJson;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "is_personalized", nullable = false)
    @NotNull
    private Boolean isPersonalized = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20, nullable = false)
    @NotNull
    private QuestSource source = QuestSource.STATIC;

    @Column(name = "source_npc_code", length = 50)
    @Size(max = 50)
    private String sourceNpcCode;

    @Column(name = "pregen_id")
    private UUID pregenId;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;
}