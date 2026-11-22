package service;

import contracts.KafkaTopics;
import contracts.PlaystyleUpdatedEvent;
import dto.PlaystyleResponse;
import dto.UpdatePlaystyleRequest;
import entity.OutboxEvent;
import entity.Playstyle;
import entity.enums.OutboxStatus;
import entity.enums.PlayerStyleTag;
import exception.BusinessException;
import exception.CommonErrorCode;
import mapper.IdentityMapper;
import repository.OutboxEventRepository;
import repository.PlaystyleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaystyleServiceImpl implements PlaystyleService {

    private final PlaystyleRepository playstyleRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final IdentityMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public PlaystyleResponse getPlaystyle(UUID playerId) {
        Playstyle playstyle = playstyleRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Playstyle not found: " + playerId));
        return mapper.toPlaystyleResponse(playstyle);
    }

    @Override
    @Transactional
    public PlaystyleResponse updatePlaystyle(UUID playerId, UpdatePlaystyleRequest request) {
        Playstyle playstyle = playstyleRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Playstyle not found: " + playerId));

        if (request.preferredCropCodes() != null) {
            playstyle.setPreferredCropCodes(request.preferredCropCodes().toArray(new String[0]));
        }
        if (request.preferredAnimalCodes() != null) {
            playstyle.setPreferredAnimalCodes(request.preferredAnimalCodes().toArray(new String[0]));
        }
        if (request.playStyleTag() != null) {
            try {
                playstyle.setPlayStyleTag(PlayerStyleTag.valueOf(request.playStyleTag().toLowerCase()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Invalid play style tag: " + request.playStyleTag());
            }
        }
        if (request.tags() != null) {
            playstyle.setTags(request.tags());
        }
        playstyle.setUpdatedAt(Instant.now());

        Playstyle saved = playstyleRepository.save(playstyle);
        publishPlaystyleUpdatedEvent(saved);

        return mapper.toPlaystyleResponse(saved);
    }

    @Override
    @Transactional
    public PlaystyleResponse updateAiProfiling(UUID playerId, String playStyleTag, List<String> crops, List<String> animals, Map<String, Object> tags) {
        Playstyle playstyle = playstyleRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Playstyle not found: " + playerId));

        if (playStyleTag != null) {
            try {
                playstyle.setPlayStyleTag(PlayerStyleTag.valueOf(playStyleTag.toLowerCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (crops != null && !crops.isEmpty()) {
            playstyle.setPreferredCropCodes(crops.toArray(new String[0]));
        }
        if (animals != null && !animals.isEmpty()) {
            playstyle.setPreferredAnimalCodes(animals.toArray(new String[0]));
        }
        if (tags != null) {
            Map<String, Object> currentTags = playstyle.getTags() != null ? new HashMap<>(playstyle.getTags()) : new HashMap<>();
            currentTags.putAll(tags);
            playstyle.setTags(currentTags);
        }
        Instant now = Instant.now();
        playstyle.setAiProfiledAt(now);
        playstyle.setUpdatedAt(now);

        Playstyle saved = playstyleRepository.save(playstyle);
        publishPlaystyleUpdatedEvent(saved);

        log.info("AI profiled playstyle for player {}", playerId);
        return mapper.toPlaystyleResponse(saved);
    }

    private void publishPlaystyleUpdatedEvent(Playstyle playstyle) {
        UUID eventId = UUID.randomUUID();
        List<String> crops = playstyle.getPreferredCropCodes() != null ? Arrays.asList(playstyle.getPreferredCropCodes()) : Collections.emptyList();
        List<String> animals = playstyle.getPreferredAnimalCodes() != null ? Arrays.asList(playstyle.getPreferredAnimalCodes()) : Collections.emptyList();
        String tag = playstyle.getPlayStyleTag() != null ? playstyle.getPlayStyleTag().name().toLowerCase() : null;

        PlaystyleUpdatedEvent event = new PlaystyleUpdatedEvent(
                eventId,
                playstyle.getPlayerId(),
                tag,
                crops,
                animals,
                playstyle.getUpdatedAt()
        );

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(eventId)
                .aggregateType("PLAYSTYLE")
                .aggregateId(playstyle.getPlayerId().toString())
                .eventType(KafkaTopics.PLAYSTYLE_UPDATED)
                .payload(objectMapper.valueToTree(event))
                .status(OutboxStatus.PENDING)
                .retryCount((short) 0)
                .createdAt(Instant.now())
                .build();

        outboxEventRepository.save(outboxEvent);
    }
}
