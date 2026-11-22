package media_service.service.impl;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import exception.BusinessException;
import exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import media_service.dto.*;
import media_service.entity.Asset;
import media_service.entity.AssetVariant;
import media_service.entity.UploadIntent;
import media_service.entity.enums.*;
import media_service.mapper.MediaMapper;
import media_service.repository.AssetRepository;
import media_service.repository.AssetVariantRepository;
import media_service.repository.UploadIntentRepository;
import media_service.service.MediaOutboxService;
import media_service.service.MediaStorageService;
import media_service.service.MediaUploadService;

import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaUploadServiceImpl implements MediaUploadService {

    @Value("${minio.buckets.catalog:farmlife-catalog}")
    private String catalogBucket;

    @Value("${minio.buckets.user:farmlife-user}")
    private String userBucket;

    @Value("${minio.buckets.temp:farmlife-temp}")
    private String tempBucket;

    private final UploadIntentRepository uploadIntentRepository;
    private final AssetRepository assetRepository;
    private final AssetVariantRepository assetVariantRepository;
    private final MediaStorageService mediaStorageService;
    private final MediaOutboxService outboxService;

    @Override
    @Transactional
    public UploadIntentResponse createUploadIntent(UUID playerId, UploadIntentRequest request) {
        String tempKey = "temp/" + playerId + "/" + UUID.randomUUID() + ".tmp";

        UploadIntent intent = UploadIntent.builder()
                .playerId(playerId)
                .purpose(request.purpose())
                .expectedType(request.expectedType())
                .maxSizeBytes(request.sizeBytes() > 0 ? request.sizeBytes() : 10 * 1024 * 1024L)
                .tempObjectKey(tempKey)
                .status(UploadStatus.PENDING)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .createdAt(Instant.now())
                .build();

        intent = uploadIntentRepository.save(intent);

        String presignedPutUrl = mediaStorageService.generatePresignedPutUrl(
                tempBucket,
                tempKey,
                Duration.ofMinutes(15),
                request.expectedType()
        );

        log.info("Created upload intent {} for player {} temp key {}", intent.getIntentId(), playerId, tempKey);
        return MediaMapper.toUploadIntentResponse(intent, presignedPutUrl);
    }

    @Override
    @Transactional
    public AssetResponse validateAndPromote(UUID playerId, UUID intentId) {
        UploadIntent intent = uploadIntentRepository.findById(intentId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Upload intent not found"));

        if (!intent.getPlayerId().equals(playerId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "Upload intent belongs to different player");
        }

        if (intent.getStatus() != UploadStatus.PENDING) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Upload intent is not in PENDING status: " + intent.getStatus());
        }

        if (intent.getExpiresAt().isBefore(Instant.now())) {
            intent.setStatus(UploadStatus.EXPIRED);
            uploadIntentRepository.save(intent);
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Upload intent has expired");
        }

        byte[] fileBytes;
        try {
            fileBytes = mediaStorageService.getObjectBytes(tempBucket, intent.getTempObjectKey());
        } catch (Exception ex) {
            log.error("Failed to retrieve file from temp bucket for intent {}", intentId, ex);
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "File not uploaded to storage yet");
        }

        // 1. Apache Tika file signature verification
        Tika tika = new Tika();
        String detectedMimeType = tika.detect(fileBytes);
        log.info("Tika detected MIME: {} for intent: {}", detectedMimeType, intentId);

        if (!detectedMimeType.startsWith("image/")) {
            intent.setStatus(UploadStatus.REJECTED);
            intent.setRejectionReason("File is not a valid image. Detected: " + detectedMimeType);
            uploadIntentRepository.save(intent);
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "File is not an image");
        }

        // 2. Compute SHA-256
        String sha256;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            sha256 = hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("SHA-256 computation failed", ex);
        }

        // 3. Process image with Scrimage
        int width = 0;
        int height = 0;
        byte[] originalWebpBytes;
        byte[] icon128Bytes = null;
        byte[] thumb256Bytes = null;

        try {
            ImmutableImage image = ImmutableImage.loader().fromBytes(fileBytes);
            width = image.width;
            height = image.height;

            originalWebpBytes = image.bytes(WebpWriter.DEFAULT);
            if (width > 128 || height > 128) {
                icon128Bytes = image.max(128, 128).bytes(WebpWriter.DEFAULT);
            }
            if (width > 256 || height > 256) {
                thumb256Bytes = image.max(256, 256).bytes(WebpWriter.DEFAULT);
            }
        } catch (Exception ex) {
            log.error("Failed to process image with Scrimage for intent {}", intentId, ex);
            intent.setStatus(UploadStatus.REJECTED);
            intent.setRejectionReason("Image parsing/compression failed: " + ex.getMessage());
            uploadIntentRepository.save(intent);
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Corrupt or unreadable image file");
        }

        // 4. Save to target MinIO bucket (Rule #7: immutable asset_key, e.g. users/{playerId}/v1/{sha256}.webp)
        String baseKey = "users/" + playerId + "/v1/" + sha256;
        String assetKey = baseKey + ".webp";

        mediaStorageService.putObject(userBucket, assetKey, originalWebpBytes, "image/webp");

        // 5. Create Asset entity
        Asset asset = Asset.builder()
                .assetKey(assetKey)
                .bucket(AssetBucket.FARMLIFE_USER)
                .sha256(sha256)
                .contentType("image/webp")
                .sizeBytes((long) originalWebpBytes.length)
                .width(width)
                .height(height)
                .assetType(AssetType.USER)
                .visibility(AssetVisibility.PUBLIC)
                .ownerService("identity-service")
                .ownerRef(playerId.toString())
                .status(AssetStatus.ACTIVE)
                .uploadedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        asset = assetRepository.save(asset);

        // 6. Save Variants
        if (icon128Bytes != null) {
            String iconKey = baseKey + "_128.webp";
            mediaStorageService.putObject(userBucket, iconKey, icon128Bytes, "image/webp");
            AssetVariant variant128 = AssetVariant.builder()
                    .assetId(asset.getAssetId())
                    .variantName("icon_128")
                    .format(VariantFormat.WEBP)
                    .assetKey(iconKey)
                    .bucket(userBucket)
                    .width(128)
                    .height(128)
                    .sizeBytes((long) icon128Bytes.length)
                    .sha256(sha256)
                    .createdAt(Instant.now())
                    .build();
            assetVariantRepository.save(variant128);
        }

        if (thumb256Bytes != null) {
            String thumbKey = baseKey + "_256.webp";
            mediaStorageService.putObject(userBucket, thumbKey, thumb256Bytes, "image/webp");
            AssetVariant variant256 = AssetVariant.builder()
                    .assetId(asset.getAssetId())
                    .variantName("thumb_256")
                    .format(VariantFormat.WEBP)
                    .assetKey(thumbKey)
                    .bucket(userBucket)
                    .width(256)
                    .height(256)
                    .sizeBytes((long) thumb256Bytes.length)
                    .sha256(sha256)
                    .createdAt(Instant.now())
                    .build();
            assetVariantRepository.save(variant256);
        }

        // 7. Update Intent
        intent.setStatus(UploadStatus.VALIDATED);
        intent.setAssetId(asset.getAssetId());
        intent.setValidatedAt(Instant.now());
        uploadIntentRepository.save(intent);

        // 8. Delete temp object
        try {
            mediaStorageService.deleteObject(tempBucket, intent.getTempObjectKey());
        } catch (Exception ex) {
            log.warn("Failed to delete temp object {}: {}", intent.getTempObjectKey(), ex.getMessage());
        }

        // 9. Publish Outbox Event asset-ready.v1
        Map<String, Object> payload = new HashMap<>();
        payload.put("assetId", asset.getAssetId().toString());
        payload.put("assetKey", asset.getAssetKey());
        payload.put("ownerService", asset.getOwnerService());
        payload.put("ownerRef", asset.getOwnerRef());
        payload.put("variants", List.of("icon_128", "thumb_256"));
        payload.put("readyAt", Instant.now().toString());

        outboxService.enqueue("ASSET", asset.getAssetId().toString(), "asset-ready.v1", payload);

        List<AssetVariant> variants = assetVariantRepository.findByAssetId(asset.getAssetId());
        log.info("Successfully validated and promoted asset {} ({})", asset.getAssetKey(), asset.getAssetId());
        return MediaMapper.toAssetResponse(asset, variants);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetResponse getAsset(String assetKey) {
        Asset asset = assetRepository.findByAssetKey(assetKey)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Asset not found: " + assetKey));
        List<AssetVariant> variants = assetVariantRepository.findByAssetId(asset.getAssetId());
        return MediaMapper.toAssetResponse(asset, variants);
    }

    @Override
    @Transactional(readOnly = true)
    public PresignedUrlResponse getPresignedDownloadUrl(String assetKey) {
        Asset asset = assetRepository.findByAssetKey(assetKey)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Asset not found: " + assetKey));

        String bucket = asset.getBucket() == AssetBucket.FARMLIFE_CATALOG ? catalogBucket : userBucket;
        String url = mediaStorageService.generatePresignedGetUrl(bucket, assetKey, Duration.ofHours(24));
        return new PresignedUrlResponse(assetKey, url, Instant.now().plus(24, ChronoUnit.HOURS));
    }
}
