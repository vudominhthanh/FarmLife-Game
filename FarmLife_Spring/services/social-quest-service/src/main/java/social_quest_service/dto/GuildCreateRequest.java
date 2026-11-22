package social_quest_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuildCreateRequest(
        @NotBlank @Size(max = 80) String name,
        String description,
        String bannerAssetKey
) {}
