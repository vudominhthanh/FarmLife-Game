package social_quest_service.dto;

import social_quest_service.entity.enums.FriendshipStatus;

import java.time.Instant;
import java.util.UUID;

public record FriendResponse(
        UUID friendPlayerId,
        FriendshipStatus status,
        UUID requestedBy,
        Instant createdAt,
        Instant updatedAt
) {}
