package ai_service.entity;

import ai_service.entity.enums.LlmPurpose;
import ai_service.entity.enums.LlmStatus;
import ai_service.entity.enums.ServiceContext;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Bảng partitioned theo tháng (PARTITION BY RANGE occurred_at).
 * Composite PK (call_id, occurred_at); call_id generate ở Java.
 */
@Entity
@Table(name = "llm_call_logs", schema = "ai")
@IdClass(LlmCallLog.LlmCallLogId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LlmCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "call_id", nullable = false, updatable = false)
    private UUID callId;

    @Id
    @Column(name = "occurred_at", nullable = false, updatable = false)
    @NotNull
    private Instant occurredAt = Instant.now();

    @Column(name = "player_id")
    private UUID playerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_context", length = 30, nullable = false)
    @NotNull
    private ServiceContext serviceContext;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 30, nullable = false)
    @NotNull
    private LlmPurpose purpose;

    @Column(name = "model", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String model;

    @Column(name = "prompt_tokens", nullable = false)
    @NotNull @PositiveOrZero
    private Integer promptTokens = 0;

    @Column(name = "completion_tokens", nullable = false)
    @NotNull @PositiveOrZero
    private Integer completionTokens = 0;

    @Column(name = "total_tokens", nullable = false)
    @NotNull @PositiveOrZero
    private Integer totalTokens = 0;

    @Column(name = "latency_ms", nullable = false)
    @NotNull @PositiveOrZero
    private Integer latencyMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private LlmStatus status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "cost_usd", nullable = false, precision = 10, scale = 6)
    @NotNull @PositiveOrZero
    @Digits(integer = 4, fraction = 6)
    private BigDecimal costUsd = BigDecimal.ZERO;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    public static class LlmCallLogId implements Serializable {
        private UUID callId;
        private Instant occurredAt;
    }
}