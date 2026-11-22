package media_service.service;

import media_service.dto.*;

import java.util.UUID;

public interface MediaUploadService {
    UploadIntentResponse createUploadIntent(UUID playerId, UploadIntentRequest request);
    AssetResponse validateAndPromote(UUID playerId, UUID intentId);
    AssetResponse getAsset(String assetKey);
    PresignedUrlResponse getPresignedDownloadUrl(String assetKey);
}
