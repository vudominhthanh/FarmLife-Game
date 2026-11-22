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
import world_service.dto.CraftQueueResponse;
import world_service.dto.CraftRequest;
import world_service.entity.CraftQueue;
import world_service.entity.OutboxEvent;
import world_service.entity.PlacedObject;
import world_service.entity.RecipeCatalog;
import world_service.entity.enums.CraftStatus;
import world_service.entity.enums.ObjectType;
import world_service.entity.enums.OutboxStatus;
import world_service.mapper.WorldMapper;
import world_service.repository.CraftQueueRepository;
import world_service.repository.OutboxEventRepository;
import world_service.repository.PlacedObjectRepository;
import world_service.repository.RecipeCatalogRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CraftingServiceImpl implements CraftingService {

    private final CraftQueueRepository craftQueueRepository;
    private final PlacedObjectRepository placedObjectRepository;
    private final RecipeCatalogRepository recipeCatalogRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final WorldMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CraftQueueResponse> getQueue(UUID farmId, UUID playerId) {
        return craftQueueRepository.findAllByPlayerId(playerId)
                .stream()
                .map(mapper::toCraftQueueResponse)
                .toList();
    }

    @Override
    @Transactional
    public CraftQueueResponse startCraft(UUID farmId, UUID playerId, CraftRequest request) {
        PlacedObject machine = placedObjectRepository.findById(request.machineObjectId())
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Machine object not found: " + request.machineObjectId()));

        if (machine.getObjectType() != ObjectType.MACHINE) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Placed object is not a machine: " + machine.getObjectType());
        }

        RecipeCatalog recipe = recipeCatalogRepository.findById(request.recipeCode())
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RECIPE_NOT_FOUND, "Recipe not found: " + request.recipeCode()));

        UUID reservationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        CraftQueue queue = CraftQueue.builder()
                .playerId(playerId)
                .machine(machine)
                .recipe(recipe)
                .quantity(request.quantity())
                .status(CraftStatus.PENDING_ITEMS)
                .reservationId(reservationId)
                .version(0L)
                .createdAt(now)
                .build();

        CraftQueue saved = craftQueueRepository.save(queue);

        // Convert recipe ingredients to ItemQuantity
        List<ItemQuantity> requiredItems = new ArrayList<>();
        if (recipe.getIngredients() != null) {
            for (Map.Entry<String, Object> entry : recipe.getIngredients().entrySet()) {
                int baseQty = 1;
                if (entry.getValue() instanceof Number n) {
                    baseQty = n.intValue();
                }
                requiredItems.add(new ItemQuantity(entry.getKey(), baseQty * request.quantity()));
            }
        }

        UUID eventId = UUID.randomUUID();
        ItemReserveRequestedEvent reserveEvent = new ItemReserveRequestedEvent(
                eventId,
                reservationId,
                playerId,
                "CRAFTING",
                saved.getQueueId(),
                requiredItems,
                Instant.now()
        );

        Map<String, Object> payload = objectMapper.convertValue(reserveEvent, new TypeReference<Map<String, Object>>() {});
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(eventId)
                .aggregateType("CRAFTING")
                .aggregateId(saved.getQueueId().toString())
                .eventType(KafkaTopics.ITEM_RESERVE_REQUESTED)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount((short) 0)
                .createdAt(now)
                .build();
        outboxEventRepository.save(outboxEvent);

        log.info("Requested crafting reservation {} for recipe {} on machine {}", reservationId, recipe.getRecipeCode(), machine.getObjectId());
        return mapper.toCraftQueueResponse(saved);
    }

    @Override
    @Transactional
    public void handleCraftReservationResolved(ReservationResolvedEvent event) {
        Optional<CraftQueue> opt = craftQueueRepository.findByReservationId(event.reservationId());
        if (opt.isEmpty()) return;

        CraftQueue queue = opt.get();
        if (event.success()) {
            OffsetDateTime now = OffsetDateTime.now();
            int craftTimeSec = queue.getRecipe().getCraftTimeSec() != null ? queue.getRecipe().getCraftTimeSec() : 60;
            int totalSec = craftTimeSec * queue.getQuantity();

            queue.setStatus(CraftStatus.IN_PROGRESS);
            queue.setStartedAt(now);
            queue.setFinishesAt(now.plusSeconds(totalSec));
            craftQueueRepository.save(queue);
            log.info("Crafting queue {} in progress, finishes at {}", queue.getQueueId(), queue.getFinishesAt());
        } else {
            queue.setStatus(CraftStatus.FAILED);
            craftQueueRepository.save(queue);
            log.warn("Crafting reservation failed for queue {}: {}", queue.getQueueId(), event.failureReason());
        }
    }
}
