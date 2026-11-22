package service;

import dto.PlayerSettingsResponse;
import dto.UpdateSettingsRequest;
import entity.PlayerSetting;
import exception.BusinessException;
import exception.CommonErrorCode;
import mapper.IdentityMapper;
import repository.PlayerSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerSettingServiceImpl implements PlayerSettingService {

    private final PlayerSettingRepository settingRepository;
    private final IdentityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PlayerSettingsResponse getSettings(UUID playerId) {
        PlayerSetting setting = settingRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Settings not found for player: " + playerId));
        return mapper.toSettingsResponse(setting);
    }

    @Override
    @Transactional
    public PlayerSettingsResponse updateSettings(UUID playerId, UpdateSettingsRequest request) {
        PlayerSetting setting = settingRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Settings not found for player: " + playerId));

        if (request.language() != null && !request.language().isBlank()) {
            setting.setLanguage(request.language());
        }
        if (request.musicVolume() != null) {
            setting.setMusicVolume(request.musicVolume());
        }
        if (request.sfxVolume() != null) {
            setting.setSfxVolume(request.sfxVolume());
        }
        if (request.notifications() != null) {
            setting.setNotifications(request.notifications());
        }
        if (request.privacy() != null) {
            setting.setPrivacy(request.privacy());
        }
        setting.setUpdatedAt(Instant.now());

        PlayerSetting saved = settingRepository.save(setting);
        return mapper.toSettingsResponse(saved);
    }
}
