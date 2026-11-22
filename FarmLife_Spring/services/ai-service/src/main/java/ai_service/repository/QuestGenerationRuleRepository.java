package ai_service.repository;

import ai_service.entity.QuestGenerationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestGenerationRuleRepository extends JpaRepository<QuestGenerationRule, Integer> {
    List<QuestGenerationRule> findByIsActiveTrueOrderByPriorityAsc();
}
