package contracts;

import java.time.Instant;
import java.util.UUID;

public record NpcStateChangedEvent(
        UUID eventId,
        String npcCode,
        String oldState,
        String newState,
        String mapCode,
        int coordX,
        int coordY,
        Instant changedAt
) {}
