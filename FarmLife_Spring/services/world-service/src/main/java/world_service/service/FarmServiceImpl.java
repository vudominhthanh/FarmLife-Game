package world_service.service;

import exception.BusinessException;
import exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world_service.dto.FarmResponse;
import world_service.dto.PlotResponse;
import world_service.entity.ActivePlanting;
import world_service.entity.Farm;
import world_service.entity.FarmPermission;
import world_service.entity.Plot;
import world_service.entity.enums.PermissionRole;
import world_service.entity.enums.Season;
import world_service.entity.enums.Weather;
import world_service.entity.id.FarmPermissionId;
import world_service.mapper.WorldMapper;
import world_service.repository.ActivePlantingRepository;
import world_service.repository.FarmPermissionRepository;
import world_service.repository.FarmRepository;
import world_service.repository.PlotRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FarmServiceImpl implements FarmService {

    private final FarmRepository farmRepository;
    private final PlotRepository plotRepository;
    private final ActivePlantingRepository activePlantingRepository;
    private final FarmPermissionRepository farmPermissionRepository;
    private final WorldMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public FarmResponse getFarmByPlayer(UUID playerId) {
        Farm farm = farmRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.FARM_NOT_FOUND, "Farm not found for player: " + playerId));
        return mapper.toFarmResponse(farm);
    }

    @Override
    @Transactional(readOnly = true)
    public FarmResponse getFarm(UUID farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.FARM_NOT_FOUND, "Farm not found: " + farmId));
        return mapper.toFarmResponse(farm);
    }

    @Override
    @Transactional
    public Farm createInitialFarmForPlayer(UUID playerId, String farmName) {
        return farmRepository.findByPlayerId(playerId).orElseGet(() -> {
            log.info("Provisioning initial farm for player {}", playerId);
            OffsetDateTime now = OffsetDateTime.now();

            Farm farm = Farm.builder()
                    .playerId(playerId)
                    .name(farmName != null && !farmName.isBlank() ? farmName : "My Farm")
                    .sizeLevel(1)
                    .gridWidth((short) 20)
                    .gridHeight((short) 20)
                    .weather(Weather.SUNNY)
                    .season(Season.SPRING)
                    .version(0L)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            Farm savedFarm = farmRepository.save(farm);

            // Create initial 6 plots
            List<Plot> starterPlots = new ArrayList<>();
            short[][] coords = {{5, 5}, {5, 6}, {5, 7}, {6, 5}, {6, 6}, {6, 7}};
            for (short[] c : coords) {
                Plot plot = Plot.builder()
                        .farm(savedFarm)
                        .coordX(c[0])
                        .coordY(c[1])
                        .soilQuality((short) 100)
                        .terrainType("soil")
                        .locked(false)
                        .build();
                starterPlots.add(plot);
            }
            plotRepository.saveAll(starterPlots);

            // Grant co-owner permission to owner
            FarmPermission permission = FarmPermission.builder()
                    .id(new FarmPermissionId(savedFarm.getFarmId(), playerId))
                    .farm(savedFarm)
                    .role(PermissionRole.CO_OWNER)
                    .canWater(true)
                    .canHarvest(true)
                    .canBuild(true)
                    .grantedAt(now)
                    .build();
            farmPermissionRepository.save(permission);

            log.info("Provisioned farm {} with 6 starter plots for player {}", savedFarm.getFarmId(), playerId);
            return savedFarm;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlotResponse> getPlots(UUID farmId) {
        List<Plot> plots = plotRepository.findAllByFarm_FarmId(farmId);
        return plots.stream().map(p -> {
            ActivePlanting planting = activePlantingRepository.findByPlot_PlotId(p.getPlotId()).orElse(null);
            return mapper.toPlotResponse(p, planting);
        }).toList();
    }
}
