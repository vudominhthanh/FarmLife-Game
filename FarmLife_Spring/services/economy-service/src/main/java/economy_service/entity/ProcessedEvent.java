package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events", schema = "economy")
@IdClass(ProcessedEvent.ProcessedEventId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessedEvent {

    @Id
    @Column(name = "event_id", nullable = false)
    @NotNull
    private UUID eventId;

    @Id
    @Column(name = "consumer_group", length = 80, nullable = false)
    @NotBlank @Size(max = 80)
    private String consumerGroup;

    @CreationTimestamp
    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    public static class ProcessedEventId implements Serializable {
        private UUID eventId;
        private String consumerGroup;
    }
}
