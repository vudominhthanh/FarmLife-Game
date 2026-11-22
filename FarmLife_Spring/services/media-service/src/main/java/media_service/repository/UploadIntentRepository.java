package media_service.repository;

import media_service.entity.UploadIntent;
import media_service.entity.enums.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UploadIntentRepository extends JpaRepository<UploadIntent, UUID> {
    Optional<UploadIntent> findByTempObjectKey(String tempObjectKey);
    List<UploadIntent> findByPlayerIdAndStatus(UUID playerId, UploadStatus status);
}
