package world_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world_service.dto.ReplayActionDto;
import world_service.dto.SyncReplayRequest;
import world_service.dto.SyncReplayResponse;
import world_service.entity.ActionReplayLog;
import world_service.entity.enums.ReplayStatus;
import world_service.entity.id.ActionReplayLogId;
import world_service.repository.ActionReplayLogRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionReplayServiceImpl implements ActionReplayService {

    private final ActionReplayLogRepository replayLogRepository;

    @Override
    @Transactional
    public SyncReplayResponse processReplay(UUID playerId, SyncReplayRequest request) {
        int accepted = 0;
        int rejected = 0;
        List<SyncReplayResponse.ReplayResultDto> results = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();

        for (ReplayActionDto action : request.actions()) {
            ActionReplayLogId id = new ActionReplayLogId(playerId, action.clientSeq());
            if (replayLogRepository.existsById(id)) {
                results.add(new SyncReplayResponse.ReplayResultDto(
                        action.clientSeq(),
                        action.actionType(),
                        "DUPLICATE",
                        "Action already processed previously"
                ));
                accepted++;
                continue;
            }

            ActionReplayLog logEntry = ActionReplayLog.builder()
                    .id(id)
                    .actionType(action.actionType())
                    .payload(action.payload() != null ? action.payload() : java.util.Collections.emptyMap())
                    .idempotencyKey(action.idempotencyKey())
                    .status(ReplayStatus.APPLIED)
                    .appliedAt(now)
                    .build();

            replayLogRepository.save(logEntry);
            results.add(new SyncReplayResponse.ReplayResultDto(
                    action.clientSeq(),
                    action.actionType(),
                    "APPLIED",
                    null
            ));
            accepted++;
        }

        log.info("Processed replay batch of {} actions for player {}: {} accepted, {} rejected",
                request.actions().size(), playerId, accepted, rejected);
        return new SyncReplayResponse(accepted, rejected, results);
    }
}
