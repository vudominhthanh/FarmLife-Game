package media_service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ValidateUploadRequest(
        @NotNull UUID intentId
) {}
