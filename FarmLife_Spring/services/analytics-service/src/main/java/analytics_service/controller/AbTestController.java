package analytics_service.controller;

import analytics_service.dto.AbAssignmentResponse;
import analytics_service.dto.AbExperimentResponse;
import analytics_service.dto.CreateExperimentRequest;
import analytics_service.entity.enums.ExperimentStatus;
import analytics_service.service.AbTestingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics/ab-tests")
@RequiredArgsConstructor
public class AbTestController {

    private final AbTestingService abTestingService;

    @PostMapping("/experiments")
    public ResponseEntity<AbExperimentResponse> createExperiment(
            @Valid @RequestBody CreateExperimentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(abTestingService.createExperiment(request));
    }

    @GetMapping("/experiments")
    public ResponseEntity<List<AbExperimentResponse>> getAllExperiments() {
        return ResponseEntity.ok(abTestingService.getAllExperiments());
    }

    @GetMapping("/experiments/{name}")
    public ResponseEntity<AbExperimentResponse> getExperimentByName(@PathVariable String name) {
        return ResponseEntity.ok(abTestingService.getExperimentByName(name));
    }

    @PatchMapping("/experiments/{name}/status")
    public ResponseEntity<AbExperimentResponse> updateExperimentStatus(
            @PathVariable String name,
            @RequestParam ExperimentStatus status
    ) {
        return ResponseEntity.ok(abTestingService.updateExperimentStatus(name, status));
    }

    @PostMapping("/assignments/evaluate/{experimentName}")
    public ResponseEntity<AbAssignmentResponse> evaluateAssignment(
            @RequestHeader("X-Player-Id") String playerIdStr,
            @PathVariable String experimentName
    ) {
        UUID playerId = UUID.fromString(playerIdStr);
        return ResponseEntity.ok(abTestingService.getOrAssignVariant(playerId, experimentName));
    }

    @GetMapping("/assignments/me")
    public ResponseEntity<List<AbAssignmentResponse>> getMyAssignments(
            @RequestHeader("X-Player-Id") String playerIdStr
    ) {
        UUID playerId = UUID.fromString(playerIdStr);
        return ResponseEntity.ok(abTestingService.getPlayerAssignments(playerId));
    }
}
