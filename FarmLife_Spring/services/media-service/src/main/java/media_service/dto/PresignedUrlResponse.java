package media_service.dto;

import java.time.Instant;

public record PresignedUrlResponse(
        String assetKey,
        String presignedGetUrl,
        Instant expiresAt
) {}
