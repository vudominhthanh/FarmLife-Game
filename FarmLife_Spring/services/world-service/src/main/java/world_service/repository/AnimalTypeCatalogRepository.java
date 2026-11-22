package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.AnimalTypeCatalog;

import java.util.List;

public interface AnimalTypeCatalogRepository extends JpaRepository<AnimalTypeCatalog, String> {
    List<AnimalTypeCatalog> findAllByActiveTrue();
}
