package entity.enums;

import exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum IdentityErrorCode implements ErrorCode {
    USERNAME_TAKEN("AUTH_USERNAME_TAKEN", "error.auth.username_taken", HttpStatus.CONFLICT),
    EMAIL_TAKEN("AUTH_EMAIL_TAKEN", "error.auth.email_taken", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS", "error.auth.invalid_credentials", HttpStatus.UNAUTHORIZED),
    ACCOUNT_INACTIVE("AUTH_ACCOUNT_INACTIVE", "error.auth.account_inactive", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED("AUTH_ACCOUNT_LOCKED", "error.auth.account_locked", HttpStatus.LOCKED),
    PLAYER_NOT_FOUND("IDENTITY_PLAYER_NOT_FOUND", "error.identity.player_not_found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;
}