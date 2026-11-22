package world_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SyncReplayRequest(
        @NotEmpty
        @Valid
        List<ReplayActionDto> actions
) {}
