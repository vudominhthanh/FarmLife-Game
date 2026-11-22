package media_service.dto;

import media_service.entity.enums.VariantFormat;

import java.util.UUID;

public record AssetVariantResponse(
        UUID variantId,
        String variantName,
        VariantFormat format,
        String assetKey,
        Integer width,
        Integer height,
        Long sizeBytes
) {}
