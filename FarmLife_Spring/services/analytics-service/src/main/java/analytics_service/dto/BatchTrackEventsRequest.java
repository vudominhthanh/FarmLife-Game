package analytics_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BatchTrackEventsRequest(
        @NotEmpty(message = "Events list must not be empty")
        List<@Valid TrackEventRequest> events
) {}
