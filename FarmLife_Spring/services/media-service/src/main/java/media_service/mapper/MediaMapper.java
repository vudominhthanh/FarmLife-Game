package media_service.mapper;

import media_service.dto.AssetResponse;
import media_service.dto.AssetVariantResponse;
import media_service.dto.UploadIntentResponse;
import media_service.entity.Asset;
import media_service.entity.AssetVariant;
import media_service.entity.UploadIntent;

import java.util.List;

public class MediaMapper {

    public static UploadIntentResponse toUploadIntentResponse(UploadIntent intent, String presignedPutUrl) {
        if (intent == null) return null;
        return new UploadIntentResponse(
                intent.getIntentId(),
                intent.getPlayerId(),
                intent.getPurpose(),
                intent.getExpectedType(),
                intent.getMaxSizeBytes(),
                intent.getTempObjectKey(),
                intent.getStatus(),
                presignedPutUrl,
                intent.getExpiresAt()
        );
    }

    public static AssetVariantResponse toAssetVariantResponse(AssetVariant v) {
        if (v == null) return null;
        return new AssetVariantResponse(
                v.getVariantId(),
                v.getVariantName(),
                v.getFormat(),
                v.getAssetKey(),
                v.getWidth(),
                v.getHeight(),
                v.getSizeBytes()
        );
    }

    public static AssetResponse toAssetResponse(Asset asset, List<AssetVariant> variants) {
        if (asset == null) return null;
        List<AssetVariantResponse> variantResponses = variants != null
                ? variants.stream().map(MediaMapper::toAssetVariantResponse).toList()
                : List.of();

        return new AssetResponse(
                asset.getAssetId(),
                asset.getAssetKey(),
                asset.getBucket(),
                asset.getContentType(),
                asset.getSizeBytes(),
                asset.getWidth(),
                asset.getHeight(),
                asset.getAssetType(),
                asset.getVisibility(),
                asset.getStatus(),
                asset.getUploadedAt(),
                variantResponses
        );
    }
}
