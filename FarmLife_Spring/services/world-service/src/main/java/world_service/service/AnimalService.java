package world_service.service;

import contracts.ReservationResolvedEvent;
import world_service.dto.AnimalResponse;
import world_service.dto.CollectProductResponse;

import java.util.List;
import java.util.UUID;

public interface AnimalService {
    List<AnimalResponse> getAnimals(UUID farmId, UUID playerId);
    AnimalResponse feedAnimal(UUID farmId, UUID animalId, UUID playerId);
    CollectProductResponse collectProduct(UUID farmId, UUID animalId, UUID playerId);
    void handleFeedingReservationResolved(ReservationResolvedEvent event);
}
