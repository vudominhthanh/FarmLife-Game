package world_service.service;

import contracts.ReservationResolvedEvent;
import world_service.dto.CraftQueueResponse;
import world_service.dto.CraftRequest;

import java.util.List;
import java.util.UUID;

public interface CraftingService {
    List<CraftQueueResponse> getQueue(UUID farmId, UUID playerId);
    CraftQueueResponse startCraft(UUID farmId, UUID playerId, CraftRequest request);
    void handleCraftReservationResolved(ReservationResolvedEvent event);
}
