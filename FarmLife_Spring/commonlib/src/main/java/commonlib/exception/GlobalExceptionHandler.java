package exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.LocaleResolver;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        Locale locale = localeResolver.resolveLocale(request);
        ErrorCode errorCode = ex.getErrorCode();

        String localizedMessage = messageSource.getMessage(
                errorCode.getMessageKey(),
                ex.getArgs(),
                errorCode.getMessageKey(),
                locale
        );

        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .errorCode(errorCode.getCode())
                .message(localizedMessage)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Locale locale = localeResolver.resolveLocale(request);
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String rawMsg = fieldError.getDefaultMessage();
            String translatedFieldMsg = (rawMsg != null)
                    ? messageSource.getMessage(rawMsg, null, rawMsg, locale)
                    : "Invalid value";
            fieldErrors.put(fieldError.getField(), translatedFieldMsg);
        }

        ErrorCode valError = CommonErrorCode.VALIDATION_FAILED;
        String mainMessage = messageSource.getMessage(
                valError.getMessageKey(), null, "Validation failed", locale
        );

        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(valError.getHttpStatus().value())
                .errorCode(valError.getCode())
                .message(mainMessage)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(valError.getHttpStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception at {}: ", request.getRequestURI(), ex);
        Locale locale = localeResolver.resolveLocale(request);
        ErrorCode internalError = CommonErrorCode.INTERNAL_ERROR;

        String localizedMessage = messageSource.getMessage(
                internalError.getMessageKey(), null, "Internal server error", locale
        );

        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(internalError.getHttpStatus().value())
                .errorCode(internalError.getCode())
                .message(localizedMessage)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(internalError.getHttpStatus()).body(body);
    }
}