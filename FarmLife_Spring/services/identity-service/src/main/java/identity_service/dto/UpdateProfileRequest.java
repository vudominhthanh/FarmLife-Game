package dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50)
        String titleCode,

        @Size(max = 2000)
        String bio
) {}
