package world_service.service;

import contracts.CropHarvestedEvent;
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
import world_service.dto.ActivePlantingResponse;
import world_service.dto.HarvestResponse;
import world_service.dto.PlantCropRequest;
import world_service.dto.PlotResponse;
import world_service.entity.*;
import world_service.entity.enums.OutboxStatus;
import world_service.entity.enums.PlantingStatus;
import world_service.mapper.WorldMapper;
import world_service.repository.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlantingServiceImpl implements PlantingService {

    private final FarmRepository farmRepository;
    private final PlotRepository plotRepository;
    private final ActivePlantingRepository activePlantingRepository;
    private final CropCatalogRepository cropCatalogRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final WorldMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ActivePlantingResponse plantCrop(UUID farmId, UUID plotId, UUID playerId, PlantCropRequest request) {
        Plot plot = plotRepository.findById(plotId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.PLOT_NOT_FOUND, "Plot not found: " + plotId));

        if (!plot.getFarm().getFarmId().equals(farmId)) {
            throw new BusinessException(CommonErrorCode.PLOT_NOT_FOUND, "Plot does not belong to farm: " + farmId);
        }
        if (plot.isLocked()) {
            throw new BusinessException(CommonErrorCode.PLOT_LOCKED, "Plot is locked");
        }

        // Check if existing planting is on plot
        Optional<ActivePlanting> existingOpt = activePlantingRepository.findByPlot_PlotId(plotId);
        if (existingOpt.isPresent()) {
            ActivePlanting existing = existingOpt.get();
            if (existing.getStatus() == PlantingStatus.GROWING || existing.getStatus() == PlantingStatus.READY || existing.getStatus() == PlantingStatus.PENDING_ITEMS) {
                throw new BusinessException(CommonErrorCode.PLOT_OCCUPIED, "Plot already has an active planting: " + existing.getStatus());
            }
            activePlantingRepository.delete(existing);
        }

        CropCatalog crop = cropCatalogRepository.findById(request.cropCode())
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Crop catalog not found: " + request.cropCode()));

        // Check season
        String farmSeason = plot.getFarm().getSeason() != null ? plot.getFarm().getSeason().name().toLowerCase() : "spring";
        if (crop.getAllowedSeasons() != null && crop.getAllowedSeasons().length > 0) {
            boolean seasonAllowed = Arrays.stream(crop.getAllowedSeasons()).anyMatch(s -> s.equalsIgnoreCase(farmSeason));
            if (!seasonAllowed) {
                throw new BusinessException(CommonErrorCode.CROP_OUT_OF_SEASON, "Crop " + crop.getCropCode() + " is out of season (" + farmSeason + ")");
            }
        }

        OffsetDateTime now = OffsetDateTime.now();
        UUID reservationId = UUID.randomUUID();

        // Calculate total growth duration
        int totalDurationSec = 0;
        int firstStageDuration = 60;
        if (crop.getStageDurations() != null && crop.getStageDurations().length > 0) {
            firstStageDuration = crop.getStageDurations()[0];
            for (int d : crop.getStageDurations()) {
                totalDurationSec += d;
            }
        } else {
            totalDurationSec = 180;
        }

        OffsetDateTime readyAt = now.plusSeconds(totalDurationSec);
        OffsetDateTime nextStageAt = now.plusSeconds(firstStageDuration);

        ActivePlanting planting = ActivePlanting.builder()
                .plot(plot)
                .crop(crop)
                .currentStage((short) 0)
                .plantedAt(now)
                .nextStageAt(nextStageAt)
                .readyAt(readyAt)
                .watered(false)
                .harvestCount((short) 0)
                .withered(false)
                .reservationId(reservationId)
                .status(PlantingStatus.PENDING_ITEMS)
                .version(0L)
                .build();

        ActivePlanting saved = activePlantingRepository.save(planting);

        // Pattern B: publish ItemReserveRequestedEvent to transactional outbox
        UUID eventId = UUID.randomUUID();
        ItemReserveRequestedEvent reserveEvent = new ItemReserveRequestedEvent(
                eventId,
                reservationId,
                playerId,
                "PLANT_CROP",
                plotId,
                List.of(new ItemQuantity(crop.getSeedItemCode(), 1)),
                Instant.now()
        );

        Map<String, Object> payload = objectMapper.convertValue(reserveEvent, new TypeReference<Map<String, Object>>() {});
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(eventId)
                .aggregateType("PLANTING")
                .aggregateId(plotId.toString())
                .eventType(KafkaTopics.ITEM_RESERVE_REQUESTED)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount((short) 0)
                .createdAt(now)
                .build();
        outboxEventRepository.save(outboxEvent);

        log.info("Initiated planting for crop {} on plot {}. Reservation requested: {}", crop.getCropCode(), plotId, reservationId);
        return mapper.toPlantingResponse(saved);
    }

    @Override
    @Transactional
    public void handleReservationResolved(ReservationResolvedEvent event) {
        log.info("Handling ReservationResolvedEvent {} for reservation {}", event.eventId(), event.reservationId());
        Optional<ActivePlanting> opt = activePlantingRepository.findByReservationId(event.reservationId());
        if (opt.isEmpty()) {
            log.warn("No active planting found for reservation {}", event.reservationId());
            return;
        }

        ActivePlanting planting = opt.get();
        if (event.success()) {
            planting.setStatus(PlantingStatus.GROWING);
            activePlantingRepository.save(planting);
            log.info("Planting on plot {} confirmed and now GROWING", planting.getPlot().getPlotId());
        } else {
            log.warn("Planting reservation failed on plot {}: {}. Deleting pending planting.", planting.getPlot().getPlotId(), event.failureReason());
            activePlantingRepository.delete(planting);
        }
    }

    @Override
    @Transactional
    public PlotResponse waterPlot(UUID farmId, UUID plotId, UUID playerId) {
        Plot plot = plotRepository.findById(plotId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.PLOT_NOT_FOUND, "Plot not found: " + plotId));

        ActivePlanting planting = activePlantingRepository.findByPlot_PlotId(plotId).orElse(null);
        if (planting != null) {
            planting.setWatered(true);
            planting.setLastWateredAt(OffsetDateTime.now());
            activePlantingRepository.save(planting);
        }

        return mapper.toPlotResponse(plot, planting);
    }

    @Override
    @Transactional
    public HarvestResponse harvestCrop(UUID farmId, UUID plotId, UUID playerId) {
        Plot plot = plotRepository.findById(plotId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.PLOT_NOT_FOUND, "Plot not found: " + plotId));

        ActivePlanting planting = activePlantingRepository.findByPlot_PlotId(plotId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "No crop planted on plot: " + plotId));

        if (planting.isWithered() || planting.getStatus() == PlantingStatus.WITHERED) {
            throw new BusinessException(CommonErrorCode.CROP_WITHERED, "Crop has withered");
        }

        OffsetDateTime now = OffsetDateTime.now();
        // Server-authoritative time check
        if (planting.getStatus() != PlantingStatus.READY && now.isBefore(planting.getReadyAt())) {
            throw new BusinessException(CommonErrorCode.CROP_NOT_READY, "Crop is not ready yet. Ready at: " + planting.getReadyAt());
        }

        CropCatalog crop = planting.getCrop();
        int baseYield = crop.getMinYield() != null ? crop.getMinYield() : 1;
        short quality = (short) Math.min(100, Math.max(50, plot.getSoilQuality()));
        int expReward = crop.getRegrowSec() != null ? 15 : 25;

        // Publish CropHarvestedEvent to outbox
        UUID eventId = UUID.randomUUID();
        CropHarvestedEvent harvestedEvent = new CropHarvestedEvent(
                eventId,
                playerId,
                farmId,
                plotId,
                crop.getHarvestItemCode(),
                baseYield,
                quality,
                expReward,
                Instant.now()
        );

        Map<String, Object> payload = objectMapper.convertValue(harvestedEvent, new TypeReference<Map<String, Object>>() {});
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(eventId)
                .aggregateType("HARVEST")
                .aggregateId(plotId.toString())
                .eventType(KafkaTopics.CROP_HARVESTED)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount((short) 0)
                .createdAt(now)
                .build();
        outboxEventRepository.save(outboxEvent);

        if (crop.getRegrowSec() != null && crop.getRegrowSec() > 0) {
            // Regrowth crop
            planting.setPlantedAt(now);
            planting.setReadyAt(now.plusSeconds(crop.getRegrowSec()));
            planting.setStatus(PlantingStatus.GROWING);
            planting.setWatered(false);
            planting.setHarvestCount((short) (planting.getHarvestCount() + 1));
            activePlantingRepository.save(planting);
        } else {
            // Non-regrow: clear plot
            activePlantingRepository.delete(planting);
        }

        log.info("Harvested crop {} on plot {} yielding {} items", crop.getCropCode(), plotId, baseYield);
        return new HarvestResponse(plotId, crop.getHarvestItemCode(), baseYield, quality, expReward);
    }
}
