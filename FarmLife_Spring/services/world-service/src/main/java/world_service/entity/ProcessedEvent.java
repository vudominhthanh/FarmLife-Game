package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import world_service.entity.id.ProcessedEventId;

@Entity
@Table(name="processed_events", schema="world")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {
    @EmbeddedId
    @NotNull
    private ProcessedEventId id;

    @Column(name="processed_at", nullable=false, updatable=false)
    private OffsetDateTime processedAt;

    @PrePersist
    void init(){if(processedAt==null) processedAt=OffsetDateTime.now();}
}