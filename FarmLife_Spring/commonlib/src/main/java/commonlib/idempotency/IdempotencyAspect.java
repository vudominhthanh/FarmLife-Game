package idempotency;

import security.ApiHeaders;
import exception.BusinessException;
import exception.CommonErrorCode;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {

    public static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(idempotent)")
    public Object handleIdempotency(
            ProceedingJoinPoint joinPoint,
            Idempotent idempotent
    ) throws Throwable {

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attrs.getRequest();

        String idempKey = request.getHeader(HEADER_IDEMPOTENCY_KEY);

        if (idempKey == null || idempKey.isBlank()) {
            throw new BusinessException(
                    CommonErrorCode.MISSING_IDEMPOTENCY_KEY
            );
        }

        String playerIdHeader = request.getHeader(ApiHeaders.PLAYER_ID);

        if (playerIdHeader == null || playerIdHeader.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing authenticated player identity"
            );
        }

        UUID playerId;

        try {
            playerId = UUID.fromString(playerIdHeader.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid player identity header",
                    ex
            );
        }

        Optional<IdempotencyKeyEntity> existingOpt =
                idempotencyKeyRepository
                        .findByIdempotencyKeyAndPlayerId(idempKey, playerId);

        if (existingOpt.isPresent()) {
            IdempotencyKeyEntity existing = existingOpt.get();

            if ("IN_PROGRESS".equals(existing.getStatus())) {
                throw new BusinessException(
                        CommonErrorCode.IDEMPOTENCY_CONFLICT
                );
            }

            return ResponseEntity.status(
                            existing.getResponseCode() != null
                                    ? existing.getResponseCode()
                                    : 200
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(existing.getResponseBody());
        }

        Instant now = Instant.now();

        IdempotencyKeyEntity record = IdempotencyKeyEntity.builder()
                .idempotencyKey(idempKey)
                .playerId(playerId)
                .endpoint(request.getMethod() + " " + request.getRequestURI())
                .status("IN_PROGRESS")
                .createdAt(now)
                .expiresAt(now.plusSeconds(idempotent.ttlSeconds()))
                .build();

        idempotencyKeyRepository.saveAndFlush(record);

        try {
            Object result = joinPoint.proceed();

            Object bodyToSerialize =
                    result instanceof ResponseEntity<?> responseEntity
                            ? responseEntity.getBody()
                            : result;

            int statusCode =
                    result instanceof ResponseEntity<?> responseEntity
                            ? responseEntity.getStatusCode().value()
                            : 200;

            // 6. Lưu response để replay
            record.setStatus("COMPLETED");
            record.setResponseCode(statusCode);
            record.setResponseBody(
                    objectMapper.writeValueAsString(bodyToSerialize)
            );

            idempotencyKeyRepository.save(record);

            return result;

        } catch (Throwable ex) {

            idempotencyKeyRepository.delete(record);

            throw ex;
        }
    }
}