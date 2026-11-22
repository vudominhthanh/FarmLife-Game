package dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username không được để trống")
        @Size(min = 3, max = 30, message = "Username phải từ 3 đến 30 ký tự")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username chỉ gồm chữ cái, số và dấu gạch dưới")
        String username,

        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 64, message = "Mật khẩu phải từ 6 đến 64 ký tự")
        String password,

        @NotBlank(message = "Tên hiển thị không được để trống")
        @Size(min = 2, max = 50, message = "Tên hiển thị từ 2 đến 50 ký tự")
        String displayName,

        String region,
        String language,
        String deviceId,
        String platform
) {}