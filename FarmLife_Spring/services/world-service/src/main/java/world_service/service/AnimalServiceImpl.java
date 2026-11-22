package world_service.service;

import contracts.ItemQuantity;
import contracts.ItemReserveRequestedEvent;
import contracts.KafkaTopics;
import contracts.ReservationResolvedEvent;
import exception.BusinessException;
import exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import world_service.dto.AnimalResponse;
import world_service.dto.CollectProductResponse;
import world_service.entity.Animal;
import world_service.entity.AnimalTypeCatalog;
import world_service.entity.OutboxEvent;
import world_service.entity.enums.OutboxStatus;
import world_service.mapper.WorldMapper;
import world_service.repository.AnimalRepository;
import world_service.repository.OutboxEventRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnimalServiceImpl implements AnimalService {

    private final AnimalRepository animalRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final WorldMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AnimalResponse> getAnimals(UUID farmId, UUID playerId) {
        return animalRepository.findAllByPlayerId(playerId)
                .stream()
                .map(mapper::toAnimalResponse)
                .toList();
    }

    @Override
    @Transactional
    public AnimalResponse feedAnimal(UUID farmId, UUID animalId, UUID playerId) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.ANIMAL_NOT_FOUND, "Animal not found: " + animalId));

        AnimalTypeCatalog type = animal.getAnimalType();
        if (type == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Animal type catalog missing");
        }

        UUID reservationId = UUID.randomUUID();
        animal.setFeedingReservationId(reservationId);
        animalRepository.save(animal);

        String feedItem = type.getFeedItemCode() != null ? type.getFeedItemCode() : "wheat";
        int feedQty = type.getFeedAmountPerDay() != null ? type.getFeedAmountPerDay() : 1;

        UUID eventId = UUID.randomUUID();
        ItemReserveRequestedEvent reserveEvent = new ItemReserveRequestedEvent(
                eventId,
                reservationId,
                playerId,
                "FEED_ANIMAL",
                animalId,
                List.of(new ItemQuantity(feedItem, feedQty)),
                Instant.now()
        );

        Map<String, Object> payload = objectMapper.convertValue(reserveEvent, new TypeReference<Map<String, Object>>() {});
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(eventId)
                .aggregateType("ANIMAL")
                .aggregateId(animalId.toString())
                .eventType(KafkaTopics.ITEM_RESERVE_REQUESTED)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount((short) 0)
                .createdAt(OffsetDateTime.now())
                .build();
        outboxEventRepository.save(outboxEvent);

        log.info("Requested feed reservation {} for animal {}", reservationId, animalId);
        return mapper.toAnimalResponse(animal);
    }

    @Override
    @Transactional
    public void handleFeedingReservationResolved(ReservationResolvedEvent event) {
        Optional<Animal> opt = animalRepository.findByFeedingReservationId(event.reservationId());
        if (opt.isEmpty()) return;

        Animal animal = opt.get();
        if (event.success()) {
            OffsetDateTime now = OffsetDateTime.now();
            animal.setHunger((short) 100);
            animal.setHappiness((short) Math.min(100, animal.getHappiness() + 10));
            animal.setLastFedAt(now);

            int intervalSec = animal.getAnimalType().getProductIntervalSec() != null ? animal.getAnimalType().getProductIntervalSec() : 300;
            animal.setLastProductAt(now.plusSeconds(intervalSec));
            animal.setFeedingReservationId(null);
            animalRepository.save(animal);
            log.info("Animal {} fed successfully, next product at {}", animal.getAnimalId(), animal.getLastProductAt());
        } else {
            animal.setFeedingReservationId(null);
            animalRepository.save(animal);
            log.warn("Feed reservation failed for animal {}: {}", animal.getAnimalId(), event.failureReason());
        }
    }

    @Override
    @Transactional
    public CollectProductResponse collectProduct(UUID farmId, UUID animalId, UUID playerId) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.ANIMAL_NOT_FOUND, "Animal not found: " + animalId));

        OffsetDateTime now = OffsetDateTime.now();
        if (animal.getLastProductAt() == null || now.isBefore(animal.getLastProductAt())) {
            throw new BusinessException(CommonErrorCode.ANIMAL_NOT_READY_FOR_PRODUCT, "Animal product is not ready yet");
        }

        AnimalTypeCatalog type = animal.getAnimalType();
        String productCode = type.getProductItemCode() != null ? type.getProductItemCode() : "egg";
        int expReward = 15;

        // Reset next product time
        int intervalSec = type.getProductIntervalSec() != null ? type.getProductIntervalSec() : 300;
        animal.setLastProductAt(now.plusSeconds(intervalSec));
        animalRepository.save(animal);

        log.info("Collected product {} from animal {}", productCode, animalId);
        return new CollectProductResponse(animalId, productCode, 1, expReward);
    }
}
