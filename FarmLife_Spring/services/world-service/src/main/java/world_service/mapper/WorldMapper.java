package world_service.mapper;

import org.springframework.stereotype.Component;
import world_service.dto.*;
import world_service.entity.*;

@Component
public class WorldMapper {

    public FarmResponse toFarmResponse(Farm farm) {
        if (farm == null) return null;
        return new FarmResponse(
                farm.getFarmId(),
                farm.getPlayerId(),
                farm.getName(),
                farm.getSizeLevel(),
                farm.getGridWidth(),
                farm.getGridHeight(),
                farm.getWeather() != null ? farm.getWeather().name().toLowerCase() : null,
                farm.getSeason() != null ? farm.getSeason().name().toLowerCase() : null,
                farm.getVersion(),
                farm.getCreatedAt(),
                farm.getUpdatedAt()
        );
    }

    public ActivePlantingResponse toPlantingResponse(ActivePlanting planting) {
        if (planting == null) return null;
        String cropCode = planting.getCrop() != null ? planting.getCrop().getCropCode() : null;
        String statusStr = planting.getStatus() != null ? planting.getStatus().name().toLowerCase() : null;
        return new ActivePlantingResponse(
                planting.getPlot() != null ? planting.getPlot().getPlotId() : null,
                cropCode,
                planting.getCurrentStage(),
                planting.getPlantedAt(),
                planting.getNextStageAt(),
                planting.getReadyAt(),
                planting.isWatered(),
                planting.getHarvestCount(),
                planting.isWithered(),
                statusStr,
                planting.getVersion()
        );
    }

    public PlotResponse toPlotResponse(Plot plot, ActivePlanting planting) {
        if (plot == null) return null;
        return new PlotResponse(
                plot.getPlotId(),
                plot.getFarm() != null ? plot.getFarm().getFarmId() : null,
                plot.getCoordX(),
                plot.getCoordY(),
                plot.getSoilQuality(),
                plot.getTerrainType(),
                plot.isLocked(),
                toPlantingResponse(planting)
        );
    }

    public PlacedObjectResponse toPlacedObjectResponse(PlacedObject obj) {
        if (obj == null) return null;
        return new PlacedObjectResponse(
                obj.getObjectId(),
                obj.getFarm() != null ? obj.getFarm().getFarmId() : null,
                obj.getObjectCode(),
                obj.getObjectType() != null ? obj.getObjectType().name().toLowerCase() : null,
                obj.getOriginX(),
                obj.getOriginY(),
                obj.getWidth(),
                obj.getHeight(),
                obj.getDirection(),
                obj.getMaxCapacity(),
                obj.getStateMetadata(),
                obj.getVersion(),
                obj.getPlacedAt()
        );
    }

    public AnimalResponse toAnimalResponse(Animal animal) {
        if (animal == null) return null;
        return new AnimalResponse(
                animal.getAnimalId(),
                animal.getPlayerId(),
                animal.getHousing() != null ? animal.getHousing().getObjectId() : null,
                animal.getAnimalType() != null ? animal.getAnimalType().getAnimalTypeCode() : null,
                animal.getName(),
                animal.getAgeSec(),
                animal.getHunger(),
                animal.getHappiness(),
                animal.getStage() != null ? animal.getStage().name().toLowerCase() : null,
                animal.getLastFedAt(),
                animal.getLastProductAt(),
                animal.getVersion(),
                animal.getCreatedAt()
        );
    }

    public CraftQueueResponse toCraftQueueResponse(CraftQueue queue) {
        if (queue == null) return null;
        return new CraftQueueResponse(
                queue.getQueueId(),
                queue.getPlayerId(),
                queue.getMachine() != null ? queue.getMachine().getObjectId() : null,
                queue.getRecipe() != null ? queue.getRecipe().getRecipeCode() : null,
                queue.getQuantity(),
                queue.getStatus() != null ? queue.getStatus().name().toLowerCase() : null,
                queue.getStartedAt(),
                queue.getFinishesAt(),
                queue.getVersion(),
                queue.getCreatedAt()
        );
    }
}
