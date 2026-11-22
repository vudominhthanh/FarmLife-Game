package world_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="breeding", schema="world")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Breeding {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="breed_id")
    private Long breedId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="parent_a")
    private Animal parentA;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="parent_b")
    private Animal parentB;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Animal child;

    @Column(nullable=false)
    private boolean success;

    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @PrePersist
    void init(){if(createdAt==null) createdAt=OffsetDateTime.now();}
}