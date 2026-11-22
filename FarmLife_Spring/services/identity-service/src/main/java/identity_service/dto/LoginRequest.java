package dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Username hoặc Email không được để trống")
        String usernameOrEmail,

        @NotBlank(message = "Mật khẩu không được để trống")
        String password,

        String deviceId,
        String platform
) {}