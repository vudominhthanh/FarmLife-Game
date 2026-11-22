package repository;

import entity.ItemCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCatalogRepository extends JpaRepository<ItemCatalog, String> {
}
