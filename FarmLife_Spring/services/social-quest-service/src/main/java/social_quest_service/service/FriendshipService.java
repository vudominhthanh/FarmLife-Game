package social_quest_service.service;

import social_quest_service.dto.FriendResponse;

import java.util.List;
import java.util.UUID;

public interface FriendshipService {
    FriendResponse sendFriendRequest(UUID senderId, UUID targetId);
    FriendResponse acceptFriendRequest(UUID currentUserId, UUID friendId);
    void declineOrRemoveFriend(UUID currentUserId, UUID friendId);
    List<FriendResponse> getFriends(UUID currentUserId);
    List<FriendResponse> getPendingRequests(UUID currentUserId);
}
