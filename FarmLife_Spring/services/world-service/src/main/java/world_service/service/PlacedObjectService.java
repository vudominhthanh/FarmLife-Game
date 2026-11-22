package world_service.service;

import world_service.dto.PlacedObjectRequest;
import world_service.dto.PlacedObjectResponse;

import java.util.List;
import java.util.UUID;

public interface PlacedObjectService {
    List<PlacedObjectResponse> getObjects(UUID farmId);
    PlacedObjectResponse placeObject(UUID farmId, UUID playerId, PlacedObjectRequest request);
    void removeObject(UUID farmId, UUID objectId, UUID playerId);
}
