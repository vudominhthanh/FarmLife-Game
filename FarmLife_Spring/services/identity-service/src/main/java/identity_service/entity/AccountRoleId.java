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
public class AccountRoleId implements Serializable {

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "role_id")
    private Short roleId;
}
