package ai_service.dto;

import jakarta.validation.constraints.NotBlank;

public record AskGuideRequest(
        @NotBlank String question,
        String category
) {}
