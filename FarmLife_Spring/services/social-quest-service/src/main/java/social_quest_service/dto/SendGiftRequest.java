package social_quest_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SendGiftRequest(
        @NotNull UUID receiverId,
        @Size(max = 50) String itemCode,
        @NotNull @Positive Integer quantity,
        String message
) {}
