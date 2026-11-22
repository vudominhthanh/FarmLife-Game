package repository;

import entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CustomizationRepository extends JpaRepository<Customization, UUID> {}