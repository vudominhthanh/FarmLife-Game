package repository;

import entity.EscrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EscrowRecordRepository extends JpaRepository<EscrowRecord, UUID> {
    Optional<EscrowRecord> findByRefId(UUID refId);
}
