package media_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import media_service.entity.enums.UploadPurpose;

public record UploadIntentRequest(
        @NotNull UploadPurpose purpose,
        @NotBlank String expectedType,
        @NotNull @Positive Long sizeBytes,
        String originalFilename
) {}
