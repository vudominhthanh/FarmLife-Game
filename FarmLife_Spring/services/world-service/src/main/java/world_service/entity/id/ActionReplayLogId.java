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
public class ActionReplayLogId implements Serializable {

    @Column(name="player_id", nullable=false)
    private UUID playerId;

    @Column(name="client_seq", nullable=false)
    @PositiveOrZero
    private Long clientSeq;

    public ActionReplayLogId() {}

    public ActionReplayLogId(UUID playerId, Long clientSeq){
        this.playerId=playerId;
        this.clientSeq=clientSeq;
    }

    @Override
    public boolean equals(Object o){if(this==o)return true;
        if(!(o instanceof ActionReplayLogId x))return false;
        return Objects.equals(playerId,x.playerId)&&Objects.equals(clientSeq,x.clientSeq);}

    @Override public int hashCode(){return Objects.hash(playerId,clientSeq);}
}
