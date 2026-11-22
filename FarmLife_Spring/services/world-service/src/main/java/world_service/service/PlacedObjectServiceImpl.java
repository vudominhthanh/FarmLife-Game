package world_service.service;

import exception.BusinessException;
import exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world_service.dto.PlacedObjectRequest;
import world_service.dto.PlacedObjectResponse;
import world_service.entity.Farm;
import world_service.entity.PlacedObject;
import world_service.entity.enums.ObjectType;
import world_service.mapper.WorldMapper;
import world_service.repository.FarmRepository;
import world_service.repository.PlacedObjectRepository;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlacedObjectServiceImpl implements PlacedObjectService {

    private final FarmRepository farmRepository;
    private final PlacedObjectRepository placedObjectRepository;
    private final WorldMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PlacedObjectResponse> getObjects(UUID farmId) {
        return placedObjectRepository.findAllByFarm_FarmId(farmId)
                .stream()
                .map(mapper::toPlacedObjectResponse)
                .toList();
    }

    @Override
    @Transactional
    public PlacedObjectResponse placeObject(UUID farmId, UUID playerId, PlacedObjectRequest request) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.FARM_NOT_FOUND, "Farm not found: " + farmId));

        ObjectType type;
        try {
            type = ObjectType.valueOf(request.objectType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Invalid object type: " + request.objectType());
        }

        short width = request.width() != null ? request.width() : 1;
        short height = request.height() != null ? request.height() : 1;
        short direction = request.direction() != null ? request.direction() : 0;

        // Check boundary
        if (request.originX() < 0 || request.originY() < 0
                || request.originX() + width > farm.getGridWidth()
                || request.originY() + height > farm.getGridHeight()) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Object exceeds farm grid boundaries: (" + farm.getGridWidth() + "x" + farm.getGridHeight() + ")");
        }

        OffsetDateTime now = OffsetDateTime.now();
        PlacedObject obj = PlacedObject.builder()
                .farm(farm)
                .objectCode(request.objectCode())
                .objectType(type)
                .originX(request.originX())
                .originY(request.originY())
                .width(width)
                .height(height)
                .direction(direction)
                .maxCapacity(request.maxCapacity())
                .stateMetadata(request.stateMetadata() != null ? request.stateMetadata() : new HashMap<>())
                .version(0L)
                .placedAt(now)
                .build();

        try {
            PlacedObject saved = placedObjectRepository.save(obj);
            log.info("Placed object {} at ({},{}) on farm {}", saved.getObjectId(), request.originX(), request.originY(), farmId);
            return mapper.toPlacedObjectResponse(saved);
        } catch (Exception e) {
            log.error("Failed to place object on farm {}", farmId, e);
            throw new BusinessException(CommonErrorCode.OBJECT_OVERLAP, "Object placement overlaps with an existing building/machine");
        }
    }

    @Override
    @Transactional
    public void removeObject(UUID farmId, UUID objectId, UUID playerId) {
        PlacedObject obj = placedObjectRepository.findById(objectId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Placed object not found: " + objectId));

        if (!obj.getFarm().getFarmId().equals(farmId)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Object does not belong to farm: " + farmId);
        }

        placedObjectRepository.delete(obj);
        log.info("Removed placed object {} from farm {}", objectId, farmId);
    }
}
