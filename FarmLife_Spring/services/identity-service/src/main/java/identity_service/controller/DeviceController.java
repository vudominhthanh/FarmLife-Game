package controller;

import dto.DeviceResponse;
import dto.RegisterDeviceRequest;
import service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/players/{playerId}/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getDevices(@PathVariable UUID playerId) {
        return ResponseEntity.ok(deviceService.getDevices(playerId));
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> registerDevice(
            @PathVariable UUID playerId,
            @Valid @RequestBody RegisterDeviceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.registerDevice(playerId, request));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> removeDevice(
            @PathVariable UUID playerId,
            @PathVariable String deviceId
    ) {
        deviceService.removeDevice(playerId, deviceId);
        return ResponseEntity.noContent().build();
    }
}
