package analytics_service.mapper;

import analytics_service.dto.*;
import analytics_service.entity.AbAssignment;
import analytics_service.entity.AbExperiment;
import analytics_service.entity.Event;
import analytics_service.entity.PlayerDailyMetric;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsMapper {

    public EventResponse toEventResponse(Event event) {
        if (event == null) return null;
        return new EventResponse(
                event.getEventId(),
                event.getPlayerId(),
                event.getEventType(),
                event.getSessionId(),
                event.getPayload(),
                event.getOccurredAt()
        );
    }

    public PlayerDailyMetricsResponse toMetricsResponse(PlayerDailyMetric metric) {
        if (metric == null) return null;
        return new PlayerDailyMetricsResponse(
                metric.getPlayerId(),
                metric.getDay(),
                metric.getSessions(),
                metric.getMinutesPlayed(),
                metric.getCoinsEarned(),
                metric.getHarvests(),
                metric.getTrades()
        );
    }

    public AbExperimentResponse toExperimentResponse(AbExperiment exp) {
        if (exp == null) return null;
        return new AbExperimentResponse(
                exp.getExperimentId(),
                exp.getName(),
                exp.getVariants(),
                exp.getStatus().name(),
                exp.getStartedAt(),
                exp.getEndedAt()
        );
    }

    public AbAssignmentResponse toAssignmentResponse(AbAssignment assignment, String experimentName) {
        if (assignment == null) return null;
        return new AbAssignmentResponse(
                assignment.getExperimentId(),
                experimentName,
                assignment.getPlayerId(),
                assignment.getVariant(),
                assignment.getAssignedAt()
        );
    }
}
