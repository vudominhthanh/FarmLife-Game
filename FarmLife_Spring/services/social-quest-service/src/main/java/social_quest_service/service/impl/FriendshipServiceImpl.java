package social_quest_service.service.impl;

import exception.BusinessException;
import exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social_quest_service.dto.FriendResponse;
import social_quest_service.entity.Friendship;
import social_quest_service.entity.enums.FriendshipStatus;
import social_quest_service.mapper.SocialQuestMapper;
import social_quest_service.repository.FriendshipRepository;
import social_quest_service.service.FriendshipService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;

    private static class CanonicalPair {
        final UUID a;
        final UUID b;

        CanonicalPair(UUID p1, UUID p2) {
            if (p1.toString().compareTo(p2.toString()) < 0) {
                this.a = p1;
                this.b = p2;
            } else {
                this.a = p2;
                this.b = p1;
            }
        }
    }

    @Override
    @Transactional
    public FriendResponse sendFriendRequest(UUID senderId, UUID targetId) {
        if (senderId.equals(targetId)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Cannot send friend request to yourself");
        }

        CanonicalPair pair = new CanonicalPair(senderId, targetId);
        Optional<Friendship> existing = friendshipRepository.findById(new Friendship.FriendshipId(pair.a, pair.b));

        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new BusinessException(CommonErrorCode.CONFLICT, "Already friends");
            }
            if (f.getStatus() == FriendshipStatus.PENDING) {
                if (f.getRequestedBy().equals(senderId)) {
                    throw new BusinessException(CommonErrorCode.CONFLICT, "Friend request already sent");
                } else {
                    // Auto accept mutual request
                    f.setStatus(FriendshipStatus.ACCEPTED);
                    f.setUpdatedAt(Instant.now());
                    friendshipRepository.save(f);
                    return SocialQuestMapper.toFriendResponse(f, senderId);
                }
            }
            f.setStatus(FriendshipStatus.PENDING);
            f.setRequestedBy(senderId);
            f.setUpdatedAt(Instant.now());
            friendshipRepository.save(f);
            return SocialQuestMapper.toFriendResponse(f, senderId);
        }

        Friendship friendship = Friendship.builder()
                .playerAId(pair.a)
                .playerBId(pair.b)
                .status(FriendshipStatus.PENDING)
                .requestedBy(senderId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        friendshipRepository.save(friendship);
        return SocialQuestMapper.toFriendResponse(friendship, senderId);
    }

    @Override
    @Transactional
    public FriendResponse acceptFriendRequest(UUID currentUserId, UUID friendId) {
        CanonicalPair pair = new CanonicalPair(currentUserId, friendId);
        Friendship friendship = friendshipRepository.findById(new Friendship.FriendshipId(pair.a, pair.b))
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Friend request not found"));

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Friendship status is not PENDING");
        }

        if (friendship.getRequestedBy().equals(currentUserId)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Cannot accept your own friend request");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setUpdatedAt(Instant.now());
        friendshipRepository.save(friendship);
        return SocialQuestMapper.toFriendResponse(friendship, currentUserId);
    }

    @Override
    @Transactional
    public void declineOrRemoveFriend(UUID currentUserId, UUID friendId) {
        CanonicalPair pair = new CanonicalPair(currentUserId, friendId);
        Friendship.FriendshipId id = new Friendship.FriendshipId(pair.a, pair.b);
        if (friendshipRepository.existsById(id)) {
            friendshipRepository.deleteById(id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponse> getFriends(UUID currentUserId) {
        return friendshipRepository.findAllByPlayerAndStatus(currentUserId, FriendshipStatus.ACCEPTED).stream()
                .map(f -> SocialQuestMapper.toFriendResponse(f, currentUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponse> getPendingRequests(UUID currentUserId) {
        return friendshipRepository.findAllByPlayerAndStatus(currentUserId, FriendshipStatus.PENDING).stream()
                .filter(f -> !f.getRequestedBy().equals(currentUserId))
                .map(f -> SocialQuestMapper.toFriendResponse(f, currentUserId))
                .toList();
    }
}
