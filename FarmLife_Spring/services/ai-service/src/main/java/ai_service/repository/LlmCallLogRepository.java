package ai_service.repository;

import ai_service.entity.LlmCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LlmCallLogRepository extends JpaRepository<LlmCallLog, LlmCallLog.LlmCallLogId> {
}
