package contracts;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AssetReadyEvent(
        UUID eventId,
        UUID assetId,
        String assetKey,
        String ownerService,
        String ownerRef,
        List<String> variants,
        Instant readyAt
) {}
