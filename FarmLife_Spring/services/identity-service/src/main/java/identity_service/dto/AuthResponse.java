package dto;

import entity.enums.PlayerStyleTag;
import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        OffsetDateTime expiresAt,
        PlayerSummary player
) {
    @Builder
    public record PlayerSummary(
            UUID playerId,
            UUID accountId,
            String username,
            String displayName,
            String region,
            String language,
            int level,
            long xp,
            String avatarCode,
            PlayerStyleTag playStyleTag
    ) {}
}