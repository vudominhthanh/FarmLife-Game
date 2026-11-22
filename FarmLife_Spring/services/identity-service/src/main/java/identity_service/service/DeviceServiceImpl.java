package service;

import dto.DeviceResponse;
import dto.RegisterDeviceRequest;
import entity.Device;
import entity.Player;
import entity.enums.Platform;
import exception.BusinessException;
import exception.CommonErrorCode;
import mapper.IdentityMapper;
import repository.DeviceRepository;
import repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final PlayerRepository playerRepository;
    private final IdentityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevices(UUID playerId) {
        return deviceRepository.findAllByPlayer_PlayerId(playerId)
                .stream()
                .map(mapper::toDeviceResponse)
                .toList();
    }

    @Override
    @Transactional
    public DeviceResponse registerDevice(UUID playerId, RegisterDeviceRequest request) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.PLAYER_NOT_FOUND, "Player not found: " + playerId));

        Platform platform;
        try {
            platform = Platform.valueOf(request.platform().toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Invalid platform: " + request.platform());
        }

        Instant now = Instant.now();
        Device device = deviceRepository.findById(request.deviceId())
                .orElse(null);

        if (device == null) {
            device = Device.builder()
                    .deviceId(request.deviceId())
                    .player(player)
                    .platform(platform)
                    .pushToken(request.pushToken())
                    .firstSeenAt(now)
                    .lastSeenAt(now)
                    .build();
        } else {
            device.setPlayer(player);
            device.setPlatform(platform);
            if (request.pushToken() != null) {
                device.setPushToken(request.pushToken());
            }
            device.setLastSeenAt(now);
        }

        Device saved = deviceRepository.save(device);
        return mapper.toDeviceResponse(saved);
    }

    @Override
    @Transactional
    public void removeDevice(UUID playerId, String deviceId) {
        Device device = deviceRepository.findByDeviceIdAndPlayer_PlayerId(deviceId, playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.DEVICE_NOT_FOUND, "Device not found for player: " + deviceId));
        deviceRepository.delete(device);
        log.info("Removed device {} for player {}", deviceId, playerId);
    }
}
