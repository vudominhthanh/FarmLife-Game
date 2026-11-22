package world_service.service;

import world_service.dto.SyncReplayRequest;
import world_service.dto.SyncReplayResponse;

import java.util.UUID;

public interface ActionReplayService {
    SyncReplayResponse processReplay(UUID playerId, SyncReplayRequest request);
}
