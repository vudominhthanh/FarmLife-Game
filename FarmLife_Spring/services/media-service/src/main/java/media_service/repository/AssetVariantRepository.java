package media_service.repository;

import media_service.entity.AssetVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetVariantRepository extends JpaRepository<AssetVariant, UUID> {
    List<AssetVariant> findByAssetId(UUID assetId);
    Optional<AssetVariant> findByAssetKey(String assetKey);
}
