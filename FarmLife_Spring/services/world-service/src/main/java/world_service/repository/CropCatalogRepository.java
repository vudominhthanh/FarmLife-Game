package world_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world_service.entity.CropCatalog;

import java.util.List;
import java.util.Optional;

public interface CropCatalogRepository extends JpaRepository<CropCatalog, String> {
    List<CropCatalog> findAllByActiveTrue();
}
