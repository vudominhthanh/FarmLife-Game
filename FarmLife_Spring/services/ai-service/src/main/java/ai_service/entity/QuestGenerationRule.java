package ai_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quest_generation_rules", schema = "ai")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuestGenerationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id", nullable = false, updatable = false)
    private Integer ruleId;

    @Column(name = "rule_name", length = 80, nullable = false)
    @NotBlank @Size(max = 80)
    private String ruleName;

    @Column(name = "priority", nullable = false)
    @NotNull
    private Integer priority = 100;

    @Column(name = "min_player_lv", nullable = false)
    @NotNull @PositiveOrZero
    private Integer minPlayerLv;

    @Column(name = "max_player_lv", nullable = false)
    @NotNull @Positive
    private Integer maxPlayerLv;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "play_style_tags", columnDefinition = "text[]")
    private List<String> playStyleTags = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_goals", nullable = false, columnDefinition = "text[]")
    @NotEmpty
    private List<String> allowedGoals;

    @Column(name = "min_target_qty", nullable = false)
    @NotNull @Positive
    private Integer minTargetQty;

    @Column(name = "max_target_qty", nullable = false)
    @NotNull @Positive
    private Integer maxTargetQty;

    @Column(name = "max_coin_reward", nullable = false)
    @NotNull @Positive
    private Long maxCoinReward;

    @Column(name = "max_xp_reward", nullable = false)
    @NotNull @Positive
    private Long maxXpReward;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_items", nullable = false, columnDefinition = "text[]")
    @NotEmpty
    private List<String> allowedItems;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @AssertTrue(message = "maxTargetQty must be >= minTargetQty")
    public boolean isTargetRangeValid() {
        return minTargetQty == null || maxTargetQty == null
                || maxTargetQty >= minTargetQty;
    }

    @AssertTrue(message = "maxPlayerLv must be >= minPlayerLv")
    public boolean isLevelRangeValid() {
        return minPlayerLv == null || maxPlayerLv == null
                || maxPlayerLv >= minPlayerLv;
    }
}