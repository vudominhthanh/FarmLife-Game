package analytics_service.service.impl;

import analytics_service.dto.AbAssignmentResponse;
import analytics_service.dto.AbExperimentResponse;
import analytics_service.dto.CreateExperimentRequest;
import analytics_service.entity.AbAssignment;
import analytics_service.entity.AbExperiment;
import analytics_service.entity.enums.ExperimentStatus;
import analytics_service.mapper.AnalyticsMapper;
import analytics_service.repository.AbAssignmentRepository;
import analytics_service.repository.AbExperimentRepository;
import analytics_service.service.AbTestingService;
import exception.BusinessException;
import exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbTestingServiceImpl implements AbTestingService {

    private final AbExperimentRepository experimentRepository;
    private final AbAssignmentRepository assignmentRepository;
    private final AnalyticsMapper analyticsMapper;

    @Override
    @Transactional
    public AbExperimentResponse createExperiment(CreateExperimentRequest request) {
        if (experimentRepository.findByName(request.name()).isPresent()) {
            throw new BusinessException(CommonErrorCode.CONFLICT, "Experiment already exists with name: " + request.name());
        }

        AbExperiment experiment = AbExperiment.builder()
                .name(request.name())
                .variants(request.variants())
                .status(ExperimentStatus.DRAFT)
                .startedAt(request.startedAt())
                .endedAt(request.endedAt())
                .build();

        AbExperiment saved = experimentRepository.save(experiment);
        log.info("Created A/B experiment '{}' with id {}", saved.getName(), saved.getExperimentId());
        return analyticsMapper.toExperimentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbExperimentResponse> getAllExperiments() {
        return experimentRepository.findAll().stream()
                .map(analyticsMapper::toExperimentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AbExperimentResponse getExperimentByName(String name) {
        AbExperiment exp = experimentRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Experiment", name));
        return analyticsMapper.toExperimentResponse(exp);
    }

    @Override
    @Transactional
    public AbExperimentResponse updateExperimentStatus(String name, ExperimentStatus status) {
        AbExperiment exp = experimentRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Experiment", name));

        exp.setStatus(status);
        if (status == ExperimentStatus.RUNNING && exp.getStartedAt() == null) {
            exp.setStartedAt(Instant.now());
        } else if (status == ExperimentStatus.ENDED && exp.getEndedAt() == null) {
            exp.setEndedAt(Instant.now());
        }

        AbExperiment saved = experimentRepository.save(exp);
        log.info("Updated experiment '{}' status to {}", name, status);
        return analyticsMapper.toExperimentResponse(saved);
    }

    @Override
    @Transactional
    public AbAssignmentResponse getOrAssignVariant(UUID playerId, String experimentName) {
        AbExperiment exp = experimentRepository.findByName(experimentName)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Experiment", experimentName));

        // Check if already assigned
        Optional<AbAssignment> existing = assignmentRepository.findByExperimentIdAndPlayerId(exp.getExperimentId(), playerId);
        if (existing.isPresent()) {
            return analyticsMapper.toAssignmentResponse(existing.get(), exp.getName());
        }

        // If not running, assign "control" or default
        String selectedVariant = "control";
        if (exp.getStatus() == ExperimentStatus.RUNNING) {
            selectedVariant = determineVariant(playerId, exp);
        }

        AbAssignment assignment = AbAssignment.builder()
                .experimentId(exp.getExperimentId())
                .playerId(playerId)
                .variant(selectedVariant)
                .assignedAt(Instant.now())
                .build();

        AbAssignment saved = assignmentRepository.save(assignment);
        log.info("Assigned player {} to variant '{}' in experiment '{}'", playerId, selectedVariant, experimentName);
        return analyticsMapper.toAssignmentResponse(saved, exp.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbAssignmentResponse> getPlayerAssignments(UUID playerId) {
        List<AbAssignment> assignments = assignmentRepository.findByPlayerId(playerId);
        List<AbAssignmentResponse> responses = new ArrayList<>(assignments.size());

        for (AbAssignment a : assignments) {
            String expName = experimentRepository.findById(a.getExperimentId())
                    .map(AbExperiment::getName)
                    .orElse("unknown");
            responses.add(analyticsMapper.toAssignmentResponse(a, expName));
        }
        return responses;
    }

    private String determineVariant(UUID playerId, AbExperiment exp) {
        Map<String, Object> variantsMap = exp.getVariants();
        if (variantsMap == null || variantsMap.isEmpty()) {
            return "control";
        }

        List<String> keys = new ArrayList<>(variantsMap.keySet());
        Collections.sort(keys);

        // Check if values are weights (numbers)
        boolean hasWeights = true;
        int totalWeight = 0;
        Map<String, Integer> weights = new LinkedHashMap<>();
        for (String k : keys) {
            Object v = variantsMap.get(k);
            if (v instanceof Number n) {
                int w = n.intValue();
                weights.put(k, w);
                totalWeight += w;
            } else {
                hasWeights = false;
                break;
            }
        }

        int hash = Math.abs((playerId.toString() + ":" + exp.getName()).hashCode());
        if (hasWeights && totalWeight > 0) {
            int bucket = hash % totalWeight;
            int cumulative = 0;
            for (Map.Entry<String, Integer> entry : weights.entrySet()) {
                cumulative += entry.getValue();
                if (bucket < cumulative) {
                    return entry.getKey();
                }
            }
        }

        // Fallback: uniform hash across keys
        return keys.get(hash % keys.size());
    }
}
