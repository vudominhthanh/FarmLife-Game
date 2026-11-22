package world_service.dto;

import java.util.List;

public record SyncReplayResponse(
        int acceptedCount,
        int rejectedCount,
        List<ReplayResultDto> results
) {
    public record ReplayResultDto(
            Long clientSeq,
            String actionType,
            String status,
            String rejectionReason
    ) {}
}
