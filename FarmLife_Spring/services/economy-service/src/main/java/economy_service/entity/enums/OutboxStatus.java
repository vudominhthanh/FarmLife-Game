package entity.enums;

public enum OutboxStatus {
    PENDING, PUBLISHED,
    FAILED, DEAD_LETTER
}