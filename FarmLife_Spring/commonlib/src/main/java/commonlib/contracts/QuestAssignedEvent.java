package contracts;

import java.time.Instant;
import java.util.UUID;

public record QuestAssignedEvent(
        UUID eventId,
        UUID playerId,
        String questCode,
        UUID pregenId,
        String sourceNpcCode,
        String title,
        String goalType,
        int goalTarget,
        String rewardJson,
        Instant assignedAt
) {}
