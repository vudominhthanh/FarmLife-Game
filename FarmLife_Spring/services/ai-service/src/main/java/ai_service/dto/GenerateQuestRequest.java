package ai_service.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateQuestRequest(
        @NotBlank String npcCode,
        String context
) {}
