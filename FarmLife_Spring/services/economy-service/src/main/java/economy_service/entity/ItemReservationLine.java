package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "item_reservation_lines", schema = "economy")
@IdClass(ItemReservationLine.ItemReservationLineId.class)
@Getter @Setter @NoArgsConstructor
public class ItemReservationLine {

    @Id
    @Column(name = "reservation_id", nullable = false)
    @NotNull
    private UUID reservationId;

    @Id
    @Column(name = "slot_id", nullable = false)
    @NotNull
    private UUID slotId;

    @Column(name = "item_code", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String itemCode;

    @Column(name = "quantity", nullable = false)
    @NotNull @Positive
    private Integer quantity;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    public static class ItemReservationLineId implements Serializable {
        private UUID reservationId;
        private UUID slotId;
    }
}