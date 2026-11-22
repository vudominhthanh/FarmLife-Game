package social_quest_service.repository;

import social_quest_service.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, ChatMessage.ChatMessageId> {
    List<ChatMessage> findTop50ByChannelOrderBySentAtDesc(String channel);
}
