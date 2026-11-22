package ai_service.dto;

import jakarta.validation.constraints.NotBlank;

public record NpcTalkRequest(
        @NotBlank String message
) {}
