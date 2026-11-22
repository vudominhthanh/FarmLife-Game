package social_quest_service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FriendRequestDto(
        @NotNull UUID targetPlayerId
) {}
