package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.RecipeCatalog;

import java.util.List;

public interface RecipeCatalogRepository extends JpaRepository<RecipeCatalog, String> {
    List<RecipeCatalog> findAllByActiveTrue();
}
