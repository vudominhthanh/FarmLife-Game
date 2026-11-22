package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "roles", schema = "identity")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Short roleId;

    @NotBlank
    @Size(max = 30)
    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String name;
}
