package analytics_service.service;

import analytics_service.dto.AbAssignmentResponse;
import analytics_service.dto.AbExperimentResponse;
import analytics_service.dto.CreateExperimentRequest;
import analytics_service.entity.enums.ExperimentStatus;

import java.util.List;
import java.util.UUID;

public interface AbTestingService {

    AbExperimentResponse createExperiment(CreateExperimentRequest request);

    List<AbExperimentResponse> getAllExperiments();

    AbExperimentResponse getExperimentByName(String name);

    AbExperimentResponse updateExperimentStatus(String name, ExperimentStatus status);

    AbAssignmentResponse getOrAssignVariant(UUID playerId, String experimentName);

    List<AbAssignmentResponse> getPlayerAssignments(UUID playerId);
}
