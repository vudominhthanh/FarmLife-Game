package media_service.repository;

import media_service.entity.CdnPurgeTask;
import media_service.entity.enums.PurgeStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CdnPurgeTaskRepository extends JpaRepository<CdnPurgeTask, Long> {
    List<CdnPurgeTask> findByStatusOrderByCreatedAtAsc(PurgeStatus status, Pageable pageable);
}
