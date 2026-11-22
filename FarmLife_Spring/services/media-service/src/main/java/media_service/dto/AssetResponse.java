package media_service.dto;

import media_service.entity.enums.AssetBucket;
import media_service.entity.enums.AssetStatus;
import media_service.entity.enums.AssetType;
import media_service.entity.enums.AssetVisibility;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AssetResponse(
        UUID assetId,
        String assetKey,
        AssetBucket bucket,
        String contentType,
        Long sizeBytes,
        Integer width,
        Integer height,
        AssetType assetType,
        AssetVisibility visibility,
        AssetStatus status,
        Instant uploadedAt,
        List<AssetVariantResponse> variants
) {}
