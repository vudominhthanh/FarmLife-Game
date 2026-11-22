package social_quest_service.service;

import social_quest_service.dto.ChatMessageResponse;
import social_quest_service.dto.SendChatMessageRequest;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    ChatMessageResponse sendMessage(UUID senderId, SendChatMessageRequest request);
    List<ChatMessageResponse> getRecentMessages(String channel);
}
