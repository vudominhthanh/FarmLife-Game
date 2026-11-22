package entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProcessedEventId implements Serializable {

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "consumer_group", length = 80)
    private String consumerGroup;
}
