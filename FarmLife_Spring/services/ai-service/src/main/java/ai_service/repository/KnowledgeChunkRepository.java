package ai_service.repository;

import ai_service.entity.KnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, UUID> {

    @Query(value = "SELECT * FROM ai.knowledge_chunks c ORDER BY c.embedding <=> cast(:embedding as vector) LIMIT :limit", nativeQuery = true)
    List<KnowledgeChunk> findSimilarChunks(@Param("embedding") String embedding, @Param("limit") int limit);

    @Query(value = "SELECT * FROM ai.knowledge_chunks c WHERE c.doc_category = :category ORDER BY c.embedding <=> cast(:embedding as vector) LIMIT :limit", nativeQuery = true)
    List<KnowledgeChunk> findSimilarChunksByCategory(@Param("category") String category, @Param("embedding") String embedding, @Param("limit") int limit);
}
