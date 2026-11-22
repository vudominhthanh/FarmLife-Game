package analytics_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bảng partitioned theo tháng (PARTITION BY RANGE occurred_at).
 * JPA map parent table — Postgres tự route insert vào partition đúng.
 * Composite PK (event_id, occurred_at); event_id generate ở Java
 * (GenerationType.UUID) để khỏi phụ thuộc DEFAULT gen_random_uuid() —
 * an toàn hơn khi Hibernate batch insert hoặc cần id trước khi flush.
 */
@Entity
@Table(name = "events", schema = "analytics")
@IdClass(Event.EventId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Id
    @Column(name = "occurred_at", nullable = false, updatable = false)
    @NotNull
    @Builder.Default
    private Instant occurredAt = Instant.now();

    @Column(name = "player_id")
    private UUID playerId;

    @Column(name = "event_type", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @NotNull
    @Builder.Default
    private Map<String, Object> payload = new HashMap<>();

    @Column(name = "session_id")
    private UUID sessionId;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    public static class EventId implements Serializable {
        private UUID eventId;
        private Instant occurredAt;
    }
}