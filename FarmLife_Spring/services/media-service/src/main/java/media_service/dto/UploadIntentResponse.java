package media_service.dto;

import media_service.entity.enums.UploadPurpose;
import media_service.entity.enums.UploadStatus;

import java.time.Instant;
import java.util.UUID;

public record UploadIntentResponse(
        UUID intentId,
        UUID playerId,
        UploadPurpose purpose,
        String expectedType,
        Long maxSizeBytes,
        String tempObjectKey,
        UploadStatus status,
        String presignedPutUrl,
        Instant expiresAt
) {}
