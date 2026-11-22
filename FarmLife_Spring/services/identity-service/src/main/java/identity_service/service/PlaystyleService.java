package service;

import dto.PlaystyleResponse;
import dto.UpdatePlaystyleRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PlaystyleService {
    PlaystyleResponse getPlaystyle(UUID playerId);
    PlaystyleResponse updatePlaystyle(UUID playerId, UpdatePlaystyleRequest request);
    PlaystyleResponse updateAiProfiling(UUID playerId, String playStyleTag, List<String> crops, List<String> animals, Map<String, Object> tags);
}
