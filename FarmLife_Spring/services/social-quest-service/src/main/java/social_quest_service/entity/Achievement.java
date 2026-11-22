package social_quest_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "achievements", schema = "social_quest")
@Getter @Setter @NoArgsConstructor
public class Achievement {

    @Id
    @Column(name = "achievement_code", length = 50, nullable = false, updatable = false)
    @NotBlank @Size(max = 50)
    private String achievementCode;

    @Column(name = "title", length = 120, nullable = false)
    @NotBlank @Size(max = 120)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "criteria_json", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> criteriaJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reward_json", nullable = false, columnDefinition = "jsonb")
    @NotNull
    private Map<String, Object> rewardJson;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;
}