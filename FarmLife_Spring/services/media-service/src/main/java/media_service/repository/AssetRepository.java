package media_service.repository;

import media_service.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    Optional<Asset> findByAssetKey(String assetKey);
    Optional<Asset> findBySha256(String sha256);
    boolean existsByAssetKey(String assetKey);
}
