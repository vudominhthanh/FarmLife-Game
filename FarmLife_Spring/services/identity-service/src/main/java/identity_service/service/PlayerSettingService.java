package service;

import dto.PlayerSettingsResponse;
import dto.UpdateSettingsRequest;

import java.util.UUID;

public interface PlayerSettingService {
    PlayerSettingsResponse getSettings(UUID playerId);
    PlayerSettingsResponse updateSettings(UUID playerId, UpdateSettingsRequest request);
}
