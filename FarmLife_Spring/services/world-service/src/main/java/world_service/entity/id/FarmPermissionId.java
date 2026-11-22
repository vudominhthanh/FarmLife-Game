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
public class FarmPermissionId implements Serializable {

    @Column(name="farm_id", nullable=false)
    private UUID farmId;

    @Column(name="player_id", nullable=false)
    private UUID playerId;

    public FarmPermissionId() {}

    public FarmPermissionId(UUID farmId, UUID playerId){
        this.farmId=farmId;
        this.playerId=playerId;
    }

    @Override public boolean equals(Object o){if(this==o)return true;if(!(o instanceof FarmPermissionId x))return false;
        return Objects.equals(farmId,x.farmId)&&Objects.equals(playerId,x.playerId);}

    @Override public int hashCode(){return Objects.hash(farmId,playerId);}
}
