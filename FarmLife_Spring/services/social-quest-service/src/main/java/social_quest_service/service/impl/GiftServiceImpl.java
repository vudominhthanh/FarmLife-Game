package social_quest_service.service.impl;

import exception.BusinessException;
import exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social_quest_service.dto.GiftResponse;
import social_quest_service.dto.SendGiftRequest;
import social_quest_service.entity.Gift;
import social_quest_service.mapper.SocialQuestMapper;
import social_quest_service.repository.GiftRepository;
import social_quest_service.service.GiftService;
import social_quest_service.service.SocialQuestOutboxService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GiftServiceImpl implements GiftService {

    private final GiftRepository giftRepository;
    private final SocialQuestOutboxService outboxService;

    @Override
    @Transactional
    public GiftResponse sendGift(UUID senderId, SendGiftRequest request) {
        if (senderId.equals(request.receiverId())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Cannot send gift to yourself");
        }

        Gift gift = Gift.builder()
                .senderId(senderId)
                .receiverId(request.receiverId())
                .itemCode(request.itemCode())
                .quantity(request.quantity())
                .message(request.message())
                .claimed(false)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .build();

        gift = giftRepository.save(gift);

        Map<String, Object> payload = new HashMap<>();
        payload.put("giftId", gift.getGiftId().toString());
        payload.put("senderId", senderId.toString());
        payload.put("receiverId", request.receiverId().toString());
        payload.put("itemCode", request.itemCode());
        payload.put("quantity", request.quantity());

        outboxService.enqueue("GIFT", gift.getGiftId().toString(), "gift-sent.v1", payload);

        return SocialQuestMapper.toGiftResponse(gift);
    }

    @Override
    @Transactional
    public GiftResponse claimGift(UUID receiverId, UUID giftId) {
        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Gift not found"));

        if (!gift.getReceiverId().equals(receiverId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "This gift is not addressed to you");
        }

        if (Boolean.TRUE.equals(gift.getClaimed())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Gift already claimed");
        }

        if (gift.getExpiresAt() != null && gift.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Gift has expired");
        }

        gift.setClaimed(true);
        gift.setClaimedAt(Instant.now());
        gift = giftRepository.save(gift);

        Map<String, Object> payload = new HashMap<>();
        payload.put("giftId", gift.getGiftId().toString());
        payload.put("receiverId", receiverId.toString());
        payload.put("itemCode", gift.getItemCode());
        payload.put("quantity", gift.getQuantity());

        outboxService.enqueue("GIFT", gift.getGiftId().toString(), "gift-claimed.v1", payload);

        return SocialQuestMapper.toGiftResponse(gift);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GiftResponse> getIncomingGifts(UUID receiverId) {
        return giftRepository.findByReceiverIdAndClaimedFalse(receiverId).stream()
                .map(SocialQuestMapper::toGiftResponse)
                .toList();
    }
}
