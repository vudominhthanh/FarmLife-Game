package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "processed_events", schema = "identity")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedEvent {

    @EmbeddedId
    private ProcessedEventId id;

    @NotNull
    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @PrePersist
    protected void onCreate() {
        if (processedAt == null) {
            processedAt = Instant.now();
        }
    }
}
