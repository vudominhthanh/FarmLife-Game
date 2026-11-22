package world_service.entity.id;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@Setter
public class ProcessedEventId implements Serializable {

    @Column(name="event_id", nullable=false)
    private UUID eventId;

    @Column(name="consumer_group", nullable=false, length=80)
    @NotBlank
    @Size(max=80)
    private String consumerGroup;

    public ProcessedEventId() {}

    public ProcessedEventId(UUID eventId,String consumerGroup){
        this.eventId=eventId;
        this.consumerGroup=consumerGroup;
    }

    @Override public boolean equals(Object o){if(this==o)return true;
        if(!(o instanceof ProcessedEventId x))return false;
        return Objects.equals(eventId,x.eventId)&&Objects.equals(consumerGroup,x.consumerGroup);}

    @Override public int hashCode(){return Objects.hash(eventId,consumerGroup);}
}
