package service;

import dto.DeviceResponse;
import dto.RegisterDeviceRequest;

import java.util.List;
import java.util.UUID;

public interface DeviceService {
    List<DeviceResponse> getDevices(UUID playerId);
    DeviceResponse registerDevice(UUID playerId, RegisterDeviceRequest request);
    void removeDevice(UUID playerId, String deviceId);
}
