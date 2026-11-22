package social_quest_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social_quest_service.dto.ChatMessageResponse;
import social_quest_service.dto.SendChatMessageRequest;
import social_quest_service.entity.ChatMessage;
import social_quest_service.mapper.SocialQuestMapper;
import social_quest_service.repository.ChatMessageRepository;
import social_quest_service.service.ChatService;
import social_quest_service.service.SocialQuestOutboxService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final SocialQuestOutboxService outboxService;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(UUID senderId, SendChatMessageRequest request) {
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .channel(request.channel())
                .content(request.content())
                .sentAt(Instant.now())
                .build();

        message = chatMessageRepository.save(message);

        Map<String, Object> payload = new HashMap<>();
        payload.put("msgId", message.getMsgId());
        payload.put("senderId", senderId.toString());
        payload.put("channel", request.channel());
        payload.put("content", request.content());
        payload.put("sentAt", message.getSentAt().toString());

        outboxService.enqueue("CHAT", request.channel(), "chat-message-sent.v1", payload);

        return SocialQuestMapper.toChatMessageResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getRecentMessages(String channel) {
        return chatMessageRepository.findTop50ByChannelOrderBySentAtDesc(channel).stream()
                .map(SocialQuestMapper::toChatMessageResponse)
                .toList();
    }
}
