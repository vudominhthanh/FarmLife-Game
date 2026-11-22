package social_quest_service.service;

import social_quest_service.dto.GiftResponse;
import social_quest_service.dto.SendGiftRequest;

import java.util.List;
import java.util.UUID;

public interface GiftService {
    GiftResponse sendGift(UUID senderId, SendGiftRequest request);
    GiftResponse claimGift(UUID receiverId, UUID giftId);
    List<GiftResponse> getIncomingGifts(UUID receiverId);
}
