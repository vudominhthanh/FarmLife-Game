package social_quest_service.repository;

import social_quest_service.entity.Guild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuildRepository extends JpaRepository<Guild, UUID> {
    Optional<Guild> findByName(String name);
    boolean existsByName(String name);
}
